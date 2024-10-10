package com.maksimowiczm.zebra.core.data.repository

import com.maksimowiczm.zebra.core.common.combineN
import com.maksimowiczm.zebra.core.data.api.model.Vault
import com.maksimowiczm.zebra.core.data.api.model.VaultBiometricsStatus
import com.maksimowiczm.zebra.core.data.api.model.VaultIdentifier
import com.maksimowiczm.zebra.core.data.api.repository.FileRepository
import com.maksimowiczm.zebra.core.data.api.repository.UserPreferencesRepository
import com.maksimowiczm.zebra.core.data.api.repository.VaultRepository
import com.maksimowiczm.zebra.core.data.utility.asVault
import com.maksimowiczm.zebra.core.database.dao.CredentialsDao
import com.maksimowiczm.zebra.core.database.dao.VaultDao
import com.maksimowiczm.zebra.core.database.model.CredentialsEntity
import com.maksimowiczm.zebra.core.database.model.VaultEntity
import kotlinx.coroutines.flow.Flow
import java.net.URI
import javax.inject.Inject

internal class VaultRepositoryImpl @Inject constructor(
    private val vaultDao: VaultDao,
    private val fileRepository: FileRepository,
    private val credentialsDao: CredentialsDao,
    private val userPreferencesRepository: UserPreferencesRepository,
) : VaultRepository {
    override fun observeVaults(): Flow<List<Vault>> {
        return combineN(
            vaultDao.observeVaults(),
            credentialsDao.observeCredentials(),
            userPreferencesRepository.observeBiometricIdentifier(),
        ) { vaults, credentials, biometricIdentifier ->
            vaults.map { vault ->
                val credential = credentials.singleOrNull { it.vaultIdentifier == vault.identifier }

                vault.asVault(
                    pathBroken = !fileRepository.isReadable(URI.create(vault.path)),
                    biometricsStatus = credential.getBiometricsStatus(biometricIdentifier),
                )
            }
        }
    }

    override fun observeVaultByIdentifier(identifier: VaultIdentifier): Flow<Vault?> {
        return combineN(
            vaultDao.observeVaultByIdentifier(identifier),
            credentialsDao.observeCredentialsByIdentifier(identifier),
            userPreferencesRepository.observeBiometricIdentifier(),
        ) { vault, credentials, biometricIdentifier ->
            vault?.asVault(
                pathBroken = !fileRepository.isReadable(URI.create(vault.path)),
                biometricsStatus = credentials.getBiometricsStatus(biometricIdentifier)
            )
        }
    }

    override suspend fun getVaultByIdentifier(identifier: VaultIdentifier): Vault? {
        val vault = vaultDao.getVaultByIdentifier(identifier) ?: return null
        val biometricIdentifier = userPreferencesRepository.getBiometricIdentifier()
        val biometricsStatus =
            credentialsDao.getCredentials(identifier).getBiometricsStatus(biometricIdentifier)

        return vault.asVault(
            pathBroken = !fileRepository.isReadable(URI.create(vault.path)),
            biometricsStatus = biometricsStatus
        )
    }

    override suspend fun vaultExistByName(name: String): Boolean = vaultDao.vaultExistByName(name)

    override suspend fun getVaultByPath(path: String): Vault? {
        val vault = vaultDao.getVaultByPath(path) ?: return null
        val biometricIdentifier = userPreferencesRepository.getBiometricIdentifier()
        val biometricsStatus =
            credentialsDao.getCredentials(vault.identifier).getBiometricsStatus(biometricIdentifier)

        return vaultDao.getVaultByPath(path)?.asVault(
            pathBroken = !fileRepository.isReadable(URI.create(path)),
            biometricsStatus = biometricsStatus
        )
    }

    override suspend fun upsertVault(name: String, path: String) {
        val entity = VaultEntity(
            name = name,
            path = path
        )

        vaultDao.upsertVault(entity)
    }

    override suspend fun deleteVault(vault: Vault) {
        vaultDao.deleteVault(vault.asEntity())
    }
}

private fun Vault.asEntity() = VaultEntity(
    name = name,
    path = path.toString(),
)

private fun CredentialsEntity?.getBiometricsStatus(
    biometricIdentifier: ByteArray,
): VaultBiometricsStatus {
    return if (this == null) {
        VaultBiometricsStatus.NotSet
    } else {
        if (cryptoIdentifier.contentEquals(biometricIdentifier)) {
            VaultBiometricsStatus.Enabled
        } else {
            VaultBiometricsStatus.Broken
        }
    }
}