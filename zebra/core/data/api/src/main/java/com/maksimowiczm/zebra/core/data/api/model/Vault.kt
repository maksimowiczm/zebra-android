package com.maksimowiczm.zebra.core.data.api.model

import android.net.Uri

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

