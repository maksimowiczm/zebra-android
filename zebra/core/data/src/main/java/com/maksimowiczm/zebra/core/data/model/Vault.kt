package com.maksimowiczm.zebra.core.data.model

import android.net.Uri
import com.maksimowiczm.zebra.core.data.repository.FileRepository
import com.maksimowiczm.zebra.core.database.model.VaultEntity

typealias VaultIdentifier = Long

data class Vault(
    val identifier: Long,
    val name: String,
    val path: Uri,
    val pathBroken: Boolean = true,
)

internal fun VaultEntity.asVault(
    fileRepository: FileRepository
): Vault = Vault(
    identifier = identifier,
    name = name,
    path = Uri.parse(path),
    pathBroken = !fileRepository.isReadable(Uri.parse(path)),
)

internal fun List<VaultEntity>.asVaults(
    fileRepository: FileRepository
): List<Vault> = map {
    it.asVault(fileRepository)
}