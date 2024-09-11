package com.maksimowiczm.zebra.core.data.model

import android.net.Uri
import com.maksimowiczm.zebra.core.data.repository.FileRepository
import com.maksimowiczm.zebra.core.database.model.VaultEntity

data class Vault(
    val name: String,
    val path: Uri,
    val pathBroken: Boolean = true,
)

internal fun VaultEntity.asVault(
    fileRepository: FileRepository
): Vault = Vault(
    name = name,
    path = Uri.parse(path),
    pathBroken = !fileRepository.isReadable(Uri.parse(path)),
)

internal fun List<VaultEntity>.asVaults(
    fileRepository: FileRepository
): List<Vault> = map {
    it.asVault(fileRepository)
}