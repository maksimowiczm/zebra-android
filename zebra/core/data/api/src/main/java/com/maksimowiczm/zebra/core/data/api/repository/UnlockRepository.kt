package com.maksimowiczm.zebra.core.data.api.repository

import com.maksimowiczm.zebra.core.data.api.model.UnsealedVaultCredentials
import com.maksimowiczm.zebra.core.data.api.model.VaultIdentifier
import com.maksimowiczm.zebra.core.data.api.model.VaultStatus
import kotlinx.coroutines.flow.Flow
import com.github.michaelbull.result.Result

sealed interface UnlockError {
    data object VaultNotFound : UnlockError
    data object FileError : UnlockError
    data object Unknown : UnlockError
}

interface UnlockRepository {
    fun observeVaultStatus(identifier: VaultIdentifier): Flow<VaultStatus>

    fun getVaultStatus(identifier: VaultIdentifier): VaultStatus

    suspend fun unlock(
        identifier: VaultIdentifier,
        credentials: UnsealedVaultCredentials,
    ): Result<Unit, UnlockError>

    fun lock(identifier: VaultIdentifier)
}