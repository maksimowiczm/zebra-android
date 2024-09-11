package com.maksimowiczm.zebra.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class VaultEntity(
    @PrimaryKey
    val name: String,
    val path: String,
)