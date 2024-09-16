package com.maksimowiczm.zebra.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.maksimowiczm.zebra.core.database.dao.CredentialsDao
import com.maksimowiczm.zebra.core.database.dao.VaultDao
import com.maksimowiczm.zebra.core.database.model.CredentialsEntity
import com.maksimowiczm.zebra.core.database.model.VaultEntity

@Database(
    entities = [
        VaultEntity::class,
        CredentialsEntity::class,
    ],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
internal abstract class ZebraDatabase : RoomDatabase() {
    abstract fun vaultDao(): VaultDao
    abstract fun credentialsDao(): CredentialsDao
}