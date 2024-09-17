package com.maksimowiczm.zebra.core.data.repository

import android.net.Uri
import com.maksimowiczm.zebra.core.common.combineN
import com.maksimowiczm.zebra.core.data.model.Vault
import com.maksimowiczm.zebra.core.data.model.VaultIdentifier
import com.maksimowiczm.zebra.core.data.model.asVault
import com.maksimowiczm.zebra.core.database.dao.CredentialsDao
import com.maksimowiczm.zebra.core.database.dao.VaultDao
import com.maksimowiczm.zebra.core.database.model.VaultEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VaultRepository @Inject constructor(
    private val vaultDao: VaultDao,
    private val fileRepository: FileRepository,
    private val credentialsDao: CredentialsDao,
) {
    fun observeVaults(): Flow<List<Vault>> {
        return combineN(
            vaultDao.observeVaults(),
            credentialsDao.observeCredentials()
        ) { vaults, credentials ->
            vaults.map { vault ->
                vault.asVault(
                    pathBroken = !fileRepository.isReadable(Uri.parse(vault.path)),
                    hasBiometrics = credentials.any { it.vaultIdentifier == vault.identifier })
            }
        }
    }

    fun observeVaultByIdentifier(identifier: VaultIdentifier): Flow<Vault?> {
        return combineN(
            vaultDao.observeVaultByIdentifier(identifier),
            credentialsDao.observeCredentialsByIdentifier(identifier)
        ) { vault, hasCredentials ->
            vault?.asVault(
                pathBroken = !fileRepository.isReadable(Uri.parse(vault.path)),
                hasBiometrics = hasCredentials
            )
        }
    }

    suspend fun getVaultByIdentifier(identifier: VaultIdentifier): Vault? {
        val vault = vaultDao.getVaultByIdentifier(identifier) ?: return null

        return vault.asVault(
            pathBroken = !fileRepository.isReadable(Uri.parse(vault.path)),
            hasBiometrics = credentialsDao.getCredentials(identifier) != null
        )
    }

    suspend fun vaultExistByName(name: String): Boolean = vaultDao.vaultExistByName(name)

    suspend fun getVaultByPath(path: String): Vault? {
        val vault = vaultDao.getVaultByPath(path) ?: return null

        return vaultDao.getVaultByPath(path)?.asVault(
            pathBroken = !fileRepository.isReadable(Uri.parse(path)),
            hasBiometrics = credentialsDao.getCredentials(vault.identifier) != null
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