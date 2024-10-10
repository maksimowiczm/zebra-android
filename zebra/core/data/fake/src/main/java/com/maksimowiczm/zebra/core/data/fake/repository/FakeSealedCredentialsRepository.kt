package com.maksimowiczm.zebra.core.data.fake.repository

import com.maksimowiczm.zebra.core.biometry.fakeIdentifier
import com.maksimowiczm.zebra.core.data.api.model.SealedVaultCredentials
import com.maksimowiczm.zebra.core.data.api.model.VaultBiometricsStatus
import com.maksimowiczm.zebra.core.data.api.model.VaultIdentifier
import com.maksimowiczm.zebra.core.data.api.repository.SealedCredentialsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeSealedCredentialsRepository @Inject constructor(
    private val fakeVaultRepository: FakeVaultRepository,
) : SealedCredentialsRepository {

    override fun observeCredentialsAvailable(vaultIdentifier: VaultIdentifier): Flow<Boolean> {
        return flow {
            emit(true)
        }
    }

    override suspend fun getCredentials(vaultIdentifier: VaultIdentifier): SealedVaultCredentials? {
        val vault = fakeVaultRepository.getVaultByIdentifier(vaultIdentifier) ?: return null

        return when (vault.biometricsStatus) {
            VaultBiometricsStatus.Broken,
            VaultBiometricsStatus.NotSet,
                -> null

            VaultBiometricsStatus.Enabled ->
                SealedVaultCredentials.Password(
                    cryptoIdentifier = fakeIdentifier,
                    data = "password".toByteArray(),
                )
        }
    }

    override suspend fun upsertCredentials(
        vaultIdentifier: VaultIdentifier,
        credentials: SealedVaultCredentials,
    ) {
    }

    override suspend fun deleteCredentials(vaultIdentifier: VaultIdentifier) {}
}