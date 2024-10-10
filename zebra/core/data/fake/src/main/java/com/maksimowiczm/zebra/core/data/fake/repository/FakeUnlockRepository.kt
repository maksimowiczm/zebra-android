package com.maksimowiczm.zebra.core.data.fake.repository

import com.maksimowiczm.zebra.core.data.api.model.UnsealedVaultCredentials
import com.maksimowiczm.zebra.core.data.api.model.VaultIdentifier
import com.maksimowiczm.zebra.core.data.api.model.VaultStatus
import com.maksimowiczm.zebra.core.data.api.repository.UnlockError
import com.maksimowiczm.zebra.core.data.api.repository.UnlockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FakeUnlockRepository @Inject constructor() : UnlockRepository {
    override fun observeVaultStatus(identifier: VaultIdentifier): Flow<VaultStatus> {
        TODO("Not yet implemented")
    }

    override fun getVaultStatus(identifier: VaultIdentifier): VaultStatus {
        TODO("Not yet implemented")
    }

    override suspend fun unlock(
        identifier: VaultIdentifier,
        credentials: UnsealedVaultCredentials,
    ): com.github.michaelbull.result.Result<Unit, UnlockError> {
        TODO("Not yet implemented")
    }

    override fun lock(identifier: VaultIdentifier) {
        TODO("Not yet implemented")
    }

}