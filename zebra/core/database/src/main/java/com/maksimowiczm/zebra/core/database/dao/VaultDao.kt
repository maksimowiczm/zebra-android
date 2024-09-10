package com.maksimowiczm.zebra.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.maksimowiczm.zebra.core.database.model.VaultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM VaultEntity")
    fun observeVaults(): Flow<List<VaultEntity>>

    @Query("SELECT EXISTS(SELECT * FROM VaultEntity WHERE name = :name)")
    suspend fun vaultExist(name: String): Boolean

    @Insert
    suspend fun insertVault(vault: VaultEntity)
}