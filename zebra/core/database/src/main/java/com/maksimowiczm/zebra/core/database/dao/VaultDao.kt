package com.maksimowiczm.zebra.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.maksimowiczm.zebra.core.database.model.VaultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM VaultEntity")
    fun observeVaults(): Flow<List<VaultEntity>>

    @Query("SELECT EXISTS(SELECT * FROM VaultEntity WHERE name = :name)")
    suspend fun vaultExistByName(name: String): Boolean

    @Query("SELECT * FROM VaultEntity WHERE path = :path")
    suspend fun getVaultByPath(path: String): VaultEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVault(vault: VaultEntity)

    @Upsert
    suspend fun upsertVault(vault: VaultEntity)

    @Delete
    suspend fun deleteVault(vault: VaultEntity)
}