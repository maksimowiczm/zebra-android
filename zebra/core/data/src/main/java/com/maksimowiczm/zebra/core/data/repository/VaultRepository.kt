package com.maksimowiczm.zebra.core.data.repository

import com.maksimowiczm.zebra.core.data.model.Vault
import com.maksimowiczm.zebra.core.data.model.asVault
import com.maksimowiczm.zebra.core.data.model.asVaults
import com.maksimowiczm.zebra.core.database.dao.VaultDao
import com.maksimowiczm.zebra.core.database.model.VaultEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VaultRepository @Inject constructor(
    private val vaultDao: VaultDao,
    private val fileRepository: FileRepository,
) {
    fun observeVaults(): Flow<List<Vault>> {
        return vaultDao.observeVaults().map { it.asVaults(fileRepository) }
    }

    suspend fun vaultExistByName(name: String): Boolean = vaultDao.vaultExistByName(name)

    suspend fun getVaultByPath(path: String): Vault? {
        return vaultDao.getVaultByPath(path)?.asVault(fileRepository)
    }

    suspend fun upsertVault(
        name: String,
        path: String
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