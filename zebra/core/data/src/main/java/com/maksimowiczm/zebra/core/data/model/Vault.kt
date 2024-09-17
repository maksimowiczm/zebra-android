package com.maksimowiczm.zebra.core.data.model

import android.net.Uri
import com.maksimowiczm.zebra.core.database.model.VaultEntity

typealias VaultIdentifier = Long

data class Vault(
    val identifier: Long,
    val name: String,
    val path: Uri,
    val pathBroken: Boolean = true,
    val biometricsStatus: VaultBiometricsStatus = VaultBiometricsStatus.NotSet,
)

sealed interface VaultBiometricsStatus {
    data object NotSet : VaultBiometricsStatus
    data object Enabled : VaultBiometricsStatus
    data object Broken : VaultBiometricsStatus
}

internal fun VaultEntity.asVault(
    pathBroken: Boolean,
    biometricsStatus: VaultBiometricsStatus,
): Vault = Vault(
    identifier = identifier,
    name = name,
    path = Uri.parse(path),
    pathBroken = pathBroken,
    biometricsStatus = biometricsStatus
)