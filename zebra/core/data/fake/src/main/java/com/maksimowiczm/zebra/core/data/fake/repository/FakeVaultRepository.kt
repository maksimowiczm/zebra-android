package com.maksimowiczm.zebra.core.data.fake.repository

import androidx.core.net.toUri
import com.maksimowiczm.zebra.core.data.api.model.Vault
import com.maksimowiczm.zebra.core.data.api.model.VaultBiometricsStatus
import com.maksimowiczm.zebra.core.data.api.model.VaultIdentifier
import com.maksimowiczm.zebra.core.data.api.repository.VaultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

val vaults = (1..100L).map {
    Vault(
        identifier = it,
        name = "Vault $it",
        path = "/path/to/vault/$it".toUri(),
        pathBroken = it % 4L == 3L,
        biometricsStatus = when (it % 3L) {
            0L -> VaultBiometricsStatus.NotSet
            1L -> VaultBiometricsStatus.Enabled
            2L -> VaultBiometricsStatus.Broken
            else -> throw IllegalStateException()
        }
    )
}

internal class FakeVaultRepository @Inject constructor() : VaultRepository {
    private val vaultsFlow: MutableStateFlow<List<Vault>> = MutableStateFlow(vaults)

    override fun observeVaults(): Flow<List<Vault>> {
        return vaultsFlow
    }

    override fun observeVaultByIdentifier(identifier: VaultIdentifier): Flow<Vault?> {
        TODO("Not yet implemented")
    }

    override suspend fun getVaultByIdentifier(identifier: VaultIdentifier): Vault? {
        TODO("Not yet implemented")
    }

    override suspend fun vaultExistByName(name: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getVaultByPath(path: String): Vault? {
        TODO("Not yet implemented")
    }

    override suspend fun upsertVault(name: String, path: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteVault(vault: Vault) {
        TODO("Not yet implemented")
    }

}