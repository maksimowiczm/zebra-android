package com.maksimowiczm.zebra.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.maksimowiczm.zebra.core.database.model.CredentialsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CredentialsDao {
    @Query("SELECT * FROM CredentialsEntity WHERE vaultIdentifier = :vaultIdentifier")
    suspend fun getCredentials(vaultIdentifier: Long): CredentialsEntity?

    @Query("SELECT EXISTS(SELECT * FROM CredentialsEntity WHERE vaultIdentifier = :vaultIdentifier)")
    fun observeCredentials(vaultIdentifier: Long): Flow<Boolean>

    @Upsert
    suspend fun upsertCredentials(credentials: CredentialsEntity)

    @Query("DELETE FROM CredentialsEntity WHERE vaultIdentifier = :vaultIdentifier")
    suspend fun deleteCredentials(vaultIdentifier: Long)
}