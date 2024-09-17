package com.maksimowiczm.zebra.core.data.repository

import android.net.Uri
import com.maksimowiczm.zebra.core.common.combineN
import com.maksimowiczm.zebra.core.data.model.Vault
import com.maksimowiczm.zebra.core.data.model.VaultBiometricsStatus
import com.maksimowiczm.zebra.core.data.model.VaultIdentifier
import com.maksimowiczm.zebra.core.data.model.asVault
import com.maksimowiczm.zebra.core.database.dao.CredentialsDao
import com.maksimowiczm.zebra.core.database.dao.VaultDao
import com.maksimowiczm.zebra.core.database.model.CredentialsEntity
import com.maksimowiczm.zebra.core.database.model.VaultEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VaultRepository @Inject constructor(
    private val vaultDao: VaultDao,
    private val fileRepository: FileRepository,
    private val credentialsDao: CredentialsDao,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    fun observeVaults(): Flow<List<Vault>> {
        return combineN(
            vaultDao.observeVaults(),
            credentialsDao.observeCredentials(),
            userPreferencesRepository.observeBiometricIdentifier(),
        ) { vaults, credentials, biometricIdentifier ->
            vaults.map { vault ->
                val credential = credentials.singleOrNull { it.vaultIdentifier == vault.identifier }

                vault.asVault(
                    pathBroken = !fileRepository.isReadable(Uri.parse(vault.path)),
                    biometricsStatus = credential.getBiometricsStatus(biometricIdentifier),
                )
            }
        }
    }

    fun observeVaultByIdentifier(identifier: VaultIdentifier): Flow<Vault?> {
        return combineN(
            vaultDao.observeVaultByIdentifier(identifier),
            credentialsDao.observeCredentialsByIdentifier(identifier),
            userPreferencesRepository.observeBiometricIdentifier(),
        ) { vault, credentials, biometricIdentifier ->
            vault?.asVault(
                pathBroken = !fileRepository.isReadable(Uri.parse(vault.path)),
                biometricsStatus = credentials.getBiometricsStatus(biometricIdentifier)
            )
        }
    }

    suspend fun getVaultByIdentifier(identifier: VaultIdentifier): Vault? {
        val vault = vaultDao.getVaultByIdentifier(identifier) ?: return null
        val biometricIdentifier = userPreferencesRepository.getBiometricIdentifier()
        val biometricsStatus =
            credentialsDao.getCredentials(identifier).getBiometricsStatus(biometricIdentifier)

        return vault.asVault(
            pathBroken = !fileRepository.isReadable(Uri.parse(vault.path)),
            biometricsStatus = biometricsStatus
        )
    }

    suspend fun vaultExistByName(name: String): Boolean = vaultDao.vaultExistByName(name)

    suspend fun getVaultByPath(path: String): Vault? {
        val vault = vaultDao.getVaultByPath(path) ?: return null
        val biometricIdentifier = userPreferencesRepository.getBiometricIdentifier()
        val biometricsStatus =
            credentialsDao.getCredentials(vault.identifier).getBiometricsStatus(biometricIdentifier)

        return vaultDao.getVaultByPath(path)?.asVault(
            pathBroken = !fileRepository.isReadable(Uri.parse(path)),
            biometricsStatus = biometricsStatus
        )
    }

    suspend fun upsertVault(
        name: String,
        path: String,
    ) {
        val entity = VaultEntity(
            name = name,
            path = path
        )

        vaultDao.upsertVault(entity)
    }

    suspend fun deleteVault(vault: Vault) {
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