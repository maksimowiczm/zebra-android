package com.maksimowiczm.zebra.core.data.model

import android.net.Uri
import com.maksimowiczm.zebra.core.database.model.VaultEntity

typealias VaultIdentifier = Long

data class Vault(
    val identifier: Long,
    val name: String,
    val path: Uri,
    val pathBroken: Boolean = true,
    val hasBiometrics: Boolean = false,
)

internal fun VaultEntity.asVault(
    pathBroken: Boolean,
    hasBiometrics: Boolean,
): Vault = Vault(
    identifier = identifier,
    name = name,
    path = Uri.parse(path),
    pathBroken = pathBroken,
    hasBiometrics = hasBiometrics
)