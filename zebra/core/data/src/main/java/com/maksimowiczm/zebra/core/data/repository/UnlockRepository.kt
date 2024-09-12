package com.maksimowiczm.zebra.core.data.repository

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.maksimowiczm.zebra.core.data.model.VaultCredentials
import com.maksimowiczm.zebra.core.data.model.VaultIdentifier
import com.maksimowiczm.zebra.core.data.model.VaultStatus
import com.maksimowiczm.zebra.core.data.source.local.keepass.KeepassDataSource
import com.maksimowiczm.zebra.core.data.source.local.keepass.UnlockError.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

sealed interface UnlockError {
    data object VaultNotFound : UnlockError
    data object FileError : UnlockError
    data object Unknown : UnlockError
}

class UnlockRepository @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val fileRepository: FileRepository,
    private val keepassDataSource: KeepassDataSource,
) {
    fun observeVaultStatus(identifier: VaultIdentifier): Flow<VaultStatus> {
        return keepassDataSource.observeDatabase(identifier)
    }

    suspend fun unlock(
        identifier: VaultIdentifier,
        credentials: VaultCredentials,
    ): Result<Unit, UnlockError> {
        val vault = vaultRepository.getVaultByIdentifier(identifier)
            ?: return Err(UnlockError.VaultNotFound)

        val stream = fileRepository.openInputStream(vault.path).getOrElse {
            return Err(UnlockError.FileError)
        }

        try {
            when (credentials) {
                is VaultCredentials.Password -> keepassDataSource.unlock(
                    identifier,
                    stream,
                    credentials.password
                )
            }.getOrElse {
                return when (it) {
                    Unknown -> Err(UnlockError.Unknown)
                    FormatError -> Err(UnlockError.FileError)
                    InvalidKey, AlreadyUnlocked -> Ok(Unit)
                }
            }
        } catch (e: CancellationException) {
            keepassDataSource.lock(identifier)
            throw e
        }

        return Ok(Unit)
    }

    fun lock(identifier: VaultIdentifier) {
        keepassDataSource.lock(identifier)
    }
}