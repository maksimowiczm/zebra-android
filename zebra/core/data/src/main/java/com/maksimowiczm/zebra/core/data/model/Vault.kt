package com.maksimowiczm.zebra.core.data.model

import com.maksimowiczm.zebra.core.database.model.VaultEntity

data class Vault(
    val name: String,
)

internal fun Vault.asVaultEntity(): VaultEntity = VaultEntity(
    name = name,
)

internal fun VaultEntity.asVault(): Vault = Vault(
    name = name,
)

internal fun List<VaultEntity>.asVaults(): List<Vault> = map(VaultEntity::asVault)