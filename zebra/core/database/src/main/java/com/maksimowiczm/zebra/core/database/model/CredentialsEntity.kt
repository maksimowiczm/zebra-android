package com.maksimowiczm.zebra.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = VaultEntity::class,
            parentColumns = ["identifier"],
            childColumns = ["vaultIdentifier"],
        )
    ]
)
data class CredentialsEntity(
    @PrimaryKey
    val vaultIdentifier: Long,
    val data: ByteArray,
    val type: CredentialsType,
)

enum class CredentialsType {
    Password
}
