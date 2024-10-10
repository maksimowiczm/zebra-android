package com.maksimowiczm.zebra.core.data.api.repository

import com.maksimowiczm.zebra.core.data.api.model.Vault
import com.maksimowiczm.zebra.core.data.api.model.VaultIdentifier
import kotlinx.coroutines.flow.Flow


interface VaultRepository {
    fun observeVaults(): Flow<List<Vault>>

    fun observeVaultByIdentifier(identifier: VaultIdentifier): Flow<Vault?>

    suspend fun getVaultByIdentifier(identifier: VaultIdentifier): Vault?

    suspend fun vaultExistByName(name: String): Boolean

    suspend fun getVaultByPath(path: String): Vault?

    suspend fun upsertVault(name: String, path: String)

    suspend fun deleteVault(vault: Vault)
}