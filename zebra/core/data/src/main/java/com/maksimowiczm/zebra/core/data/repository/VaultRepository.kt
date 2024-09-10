package com.maksimowiczm.zebra.core.data.repository

import com.maksimowiczm.zebra.core.data.model.Vault
import com.maksimowiczm.zebra.core.data.model.asVaultEntity
import com.maksimowiczm.zebra.core.data.model.asVaults
import com.maksimowiczm.zebra.core.database.dao.VaultDao
import com.maksimowiczm.zebra.core.database.model.VaultEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VaultRepository @Inject constructor(
    private val vaultDao: VaultDao,
) {
    fun observeVaults(): Flow<List<Vault>> {
        return vaultDao.observeVaults().map(List<VaultEntity>::asVaults)
    }

    suspend fun vaultExist(name: String) = vaultDao.vaultExist(name)

    suspend fun insertVault(vault: Vault) {
        vaultDao.insertVault(vault.asVaultEntity())
    }
}