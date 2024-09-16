package com.maksimowiczm.zebra.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.maksimowiczm.zebra.core.database.dao.VaultDao
import com.maksimowiczm.zebra.core.database.model.VaultEntity

@Database(
    entities = [
        VaultEntity::class,
    ],
    version = 1,
)
internal abstract class ZebraDatabase : RoomDatabase() {
    abstract fun vaultDao(): VaultDao
}