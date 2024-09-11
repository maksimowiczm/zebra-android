package com.maksimowiczm.zebra.core.data.model

import android.net.Uri
import com.maksimowiczm.zebra.core.data.repository.FileRepository
import com.maksimowiczm.zebra.core.database.model.VaultEntity

data class Vault(
    val name: String,
    val path: Uri,
)

internal fun Vault.asVaultEntity(): VaultEntity = VaultEntity(
    name = name,
    path = path.toString(),
)

internal fun VaultEntity.asVault(): Vault = Vault(
    name = name,
    path = Uri.parse(path),
)

internal fun List<VaultEntity>.asVaults(): List<Vault> = map(VaultEntity::asVault)