package com.maksimowiczm.zebra.core.data.source.local.keepass

import android.util.Log
import app.keemobile.kotpass.cryptography.EncryptedValue
import app.keemobile.kotpass.database.Credentials
import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.database.decode
import app.keemobile.kotpass.database.getEntries
import app.keemobile.kotpass.errors.CryptoError
import app.keemobile.kotpass.models.Entry
import com.maksimowiczm.zebra.core.data.model.VaultEntry
import com.maksimowiczm.zebra.core.data.model.VaultStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * In memory data source for KeePass databases.
 */
class KeepassDataSource(
    private val defaultDispatcher: CoroutineDispatcher, // CPU bound dispatcher
) {
    private val databaseFlow = MutableStateFlow<Map<Long, VaultStatus>>(emptyMap())

    /**
     * Observe [VaultStatus] for [identifier]. If database is not found, return [VaultStatus.Locked].
     */
    fun observeDatabase(identifier: Long) =
        databaseFlow.map { it.getOrDefault(identifier, VaultStatus.Locked) }

    fun getDatabaseStatus(identifier: Long): VaultStatus {
        return databaseFlow.value[identifier] ?: return VaultStatus.Locked
    }

    fun lock(identifier: Long) {
        // drop database from map
        databaseFlow.update { it.minus(identifier) }
    }

    /**
     * Start unlocking the database from the [inputStream] with the [password].
     * Consumes the input stream.
     */
    suspend fun unlock(
        identifier: Long,
        inputStream: InputStream,
        password: String,
    ) {
        if (databaseFlow.value[identifier] is VaultStatus.Unlocked) {
            return
        }

        databaseFlow.update {
            it.toMutableMap().apply {
                this[identifier] = VaultStatus.Unlocking
            }
        }
        val credentials = Credentials.from(EncryptedValue.fromString(password))

        // This works kinda bad, decode cant be cancelled.
        // Async block will run until decode is done.
        withContext(defaultDispatcher) {
            Log.d(TAG, "Attempting to unlock database")
            try {
                async {
                    try {
                        val database = KeePassDatabase.decode(inputStream, credentials)
                        ensureActive()
                        databaseFlow.update {
                            it.toMutableMap().apply {
                                this[identifier] = database.asUnlocked()
                            }
                        }
                    } catch (_: CryptoError.InvalidKey) {
                        Log.d(TAG, "Invalid key")
                        ensureActive()
                        databaseFlow.update {
                            val entry = it[identifier]
                            val count = if (entry is VaultStatus.CredentialsFailed) {
                                entry.count + 1
                            } else {
                                1
                            }

                            it.toMutableMap().apply {
                                this[identifier] = VaultStatus.CredentialsFailed(count)
                            }
                        }
                    } catch (e: CancellationException) {
                        // do not catch cancellation
                        throw e
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to unlock database", e)

                        databaseFlow.update {
                            it.toMutableMap().apply {
                                this[identifier] = VaultStatus.UnrecoverableError
                            }
                        }
                    } finally {
                        withContext(NonCancellable) {
                            Log.d(TAG, "Closing input stream")
                            inputStream.close()
                        }
                    }
                }.await()
            } catch (e: CancellationException) {
                Log.d(TAG, "Unlock cancelled")
                databaseFlow.update { map ->
                    when (map[identifier]) {
                        VaultStatus.Unlocking -> {
                            map.toMutableMap().apply {
                                this[identifier] = VaultStatus.Locked
                            }
                        }

                        VaultStatus.Locked,
                        is VaultStatus.CredentialsFailed,
                        VaultStatus.UnrecoverableError,
                        is VaultStatus.Unlocked,
                        null,
                        -> map
                    }
                }
                throw e
            }
        }
    }

    companion object {
        private const val TAG = "KeepassDataSource"
    }
}

private fun KeePassDatabase.asUnlocked(): VaultStatus.Unlocked {
    val entries = getEntries { true }
        .flatMap { (_, entries) -> entries }
        .map(Entry::toVaultEntry)

    val result = VaultStatus.Unlocked(
        entries = entries
    )

    return result
}

private fun Entry.toVaultEntry(): VaultEntry {
    val username = if (fields.userName?.content.isNullOrEmpty()) {
        null
    } else {
        fields.userName?.content
    }

    val url = if (fields.url?.content.isNullOrEmpty()) {
        null
    } else {
        fields.url?.content
    }

    return VaultEntry(
        title = fields.title?.content ?: url ?: username ?: "<untitled>",
        username = username,
        password = if (fields.password == null) {
            null
        } else {
            { fields.password!!.content }
        },
        url = url,
    )
}