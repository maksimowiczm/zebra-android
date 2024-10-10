package com.maksimowiczm.zebra.core.data.fake.repository

import com.maksimowiczm.zebra.core.data.api.model.Vault
import com.maksimowiczm.zebra.core.data.api.model.VaultBiometricsStatus
import com.maksimowiczm.zebra.core.data.api.model.VaultIdentifier
import com.maksimowiczm.zebra.core.data.api.repository.VaultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import java.net.URI
import javax.inject.Inject

private var vaults = (1..100L).map {
    Vault(
        identifier = it,
        name = "Vault $it",
        path = URI.create("/path/to/vault/$it"),
        pathBroken = it % 4L == 3L,
        biometricsStatus = when (it % 2L) {
            0L -> VaultBiometricsStatus.NotSet
            1L -> VaultBiometricsStatus.Enabled
            else -> throw IllegalStateException()
        }
    )
}
private val vaultsFlow: MutableStateFlow<List<Vault>> = MutableStateFlow(vaults)

class FakeVaultRepository @Inject constructor() : VaultRepository {
    override fun observeVaults(): Flow<List<Vault>> {
        return vaultsFlow
    }

    override fun observeVaultByIdentifier(identifier: VaultIdentifier): Flow<Vault?> {
        return flow { emit(vaults.find { it.identifier == identifier }) }
    }

    override suspend fun getVaultByIdentifier(identifier: VaultIdentifier): Vault? {
        return vaults.find { it.identifier == identifier }
    }

    override suspend fun vaultExistByName(name: String): Boolean {
        return vaults.any { it.name == name }
    }

    override suspend fun getVaultByPath(path: String): Vault? {
        return vaults.find { it.path.toString() == path }
    }

    override suspend fun upsertVault(name: String, path: String) {
        val identifier = vaults.maxOfOrNull { it.identifier }?.plus(1L) ?: 1L

        vaults = vaults.plus(
            Vault(
                identifier = identifier,
                name = name,
                path = URI.create(path),
                pathBroken = false,
                biometricsStatus = VaultBiometricsStatus.NotSet
            )
        )

        vaultsFlow.emit(vaults)
    }

    override suspend fun deleteVault(vault: Vault) {
        vaults = vaults.filter { it.identifier != vault.identifier }
        vaultsFlow.emit(vaults)
    }
}