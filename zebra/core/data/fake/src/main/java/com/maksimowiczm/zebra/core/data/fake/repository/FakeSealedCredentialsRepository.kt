package com.maksimowiczm.zebra.core.data.fake.repository

import com.maksimowiczm.zebra.core.data.api.model.SealedVaultCredentials
import com.maksimowiczm.zebra.core.data.api.model.VaultIdentifier
import com.maksimowiczm.zebra.core.data.api.repository.SealedCredentialsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FakeSealedCredentialsRepository @Inject constructor() : SealedCredentialsRepository {
    override fun observeCredentialsAvailable(vaultIdentifier: VaultIdentifier): Flow<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun getCredentials(vaultIdentifier: VaultIdentifier): SealedVaultCredentials? {
        TODO("Not yet implemented")
    }

    override suspend fun upsertCredentials(
        vaultIdentifier: VaultIdentifier,
        credentials: SealedVaultCredentials,
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteCredentials(vaultIdentifier: VaultIdentifier) {
        TODO("Not yet implemented")
    }
}