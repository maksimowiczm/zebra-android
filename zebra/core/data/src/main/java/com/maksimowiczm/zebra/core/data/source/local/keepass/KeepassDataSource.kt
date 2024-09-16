package com.maksimowiczm.zebra.core.data.source.local.keepass

import app.keemobile.kotpass.cryptography.EncryptedValue
import app.keemobile.kotpass.database.Credentials
import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.database.decode
import app.keemobile.kotpass.database.getEntries
import app.keemobile.kotpass.errors.CryptoError
import app.keemobile.kotpass.models.Entry
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.model.VaultEntry
import com.maksimowiczm.zebra.core.data.model.VaultStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.InputStream

sealed interface UnlockError {
    data object FormatError : UnlockError
    data object InvalidKey : UnlockError
    data object Unknown : UnlockError
    data object AlreadyUnlocked : UnlockError
}

/**
 * In memory data source for KeePass databases.
 */
class KeepassDataSource(
    private val ioDispatcher: CoroutineDispatcher,
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
     * Unlock database from [inputStream] with [password].
     * Consumes input stream.
     */
    suspend fun unlock(
        identifier: Long,
        inputStream: InputStream,
        password: String,
    ): Result<Unit, UnlockError> {
        val entry = databaseFlow.value[identifier]
        if (entry is VaultStatus.Unlocked) {
            return Err(UnlockError.AlreadyUnlocked)
        }

        val credentials = Credentials.from(EncryptedValue.fromString(password))
        return try {
            withContext(ioDispatcher) {
                databaseFlow.update {
                    it.toMutableMap().apply {
                        this[identifier] = VaultStatus.Unlocking
                    }
                }

                val database = KeePassDatabase.decode(inputStream, credentials)

                databaseFlow.update {
                    it.toMutableMap().apply {
                        this[identifier] = database.asUnlocked()
                    }
                }
            }

            Ok(Unit)
        } catch (_: CryptoError.InvalidKey) {
            databaseFlow.update {
                val entry = it[identifier]
                val count = if (entry is VaultStatus.Failed) {
                    entry.count + 1
                } else {
                    1
                }

                it.toMutableMap().apply {
                    this[identifier] = VaultStatus.Failed(count)
                }
            }

            Err(UnlockError.InvalidKey)
        } catch (e: Exception) {
            databaseFlow.update {
                it.toMutableMap().apply {
                    this[identifier] = VaultStatus.UnrecoverableError
                }
            }

            Err(UnlockError.FormatError)
        } finally {
            withContext(ioDispatcher + NonCancellable) {
                inputStream.close()
            }
        }
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