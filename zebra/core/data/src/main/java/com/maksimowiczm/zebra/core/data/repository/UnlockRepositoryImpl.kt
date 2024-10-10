package com.maksimowiczm.zebra.core.data.repository

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.maksimowiczm.zebra.core.data.api.model.UnsealedVaultCredentials
import com.maksimowiczm.zebra.core.data.api.model.VaultIdentifier
import com.maksimowiczm.zebra.core.data.api.model.VaultStatus
import com.maksimowiczm.zebra.core.data.api.repository.FileRepository
import com.maksimowiczm.zebra.core.data.api.repository.UnlockError
import com.maksimowiczm.zebra.core.data.api.repository.UnlockRepository
import com.maksimowiczm.zebra.core.data.api.repository.VaultRepository
import com.maksimowiczm.zebra.core.data.source.local.keepass.KeepassDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


internal class UnlockRepositoryImpl @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val fileRepository: FileRepository,
    private val keepassDataSource: KeepassDataSource,
) : UnlockRepository {
    override fun observeVaultStatus(identifier: VaultIdentifier): Flow<VaultStatus> {
        return keepassDataSource.observeDatabase(identifier)
    }

    override fun getVaultStatus(identifier: VaultIdentifier): VaultStatus {
        return keepassDataSource.getDatabaseStatus(identifier)
    }

    override suspend fun unlock(
        identifier: VaultIdentifier,
        credentials: UnsealedVaultCredentials,
    ): Result<Unit, UnlockError> {
        val vault = vaultRepository.getVaultByIdentifier(
            identifier
        )
            ?: return Err(UnlockError.VaultNotFound)

        val stream = fileRepository.openInputStream(vault.path).getOrElse {
            return Err(UnlockError.FileError)
        }

        when (credentials) {
            is UnsealedVaultCredentials.Password -> keepassDataSource.unlock(
                identifier,
                stream,
                credentials.password
            )
        }

        return Ok(Unit)
    }

    override fun lock(identifier: VaultIdentifier) {
        keepassDataSource.lock(identifier)
    }
}