package com.maksimowiczm.zebra.core.data.utility

import android.net.Uri
import com.maksimowiczm.zebra.core.data.api.model.Vault
import com.maksimowiczm.zebra.core.data.api.model.VaultBiometricsStatus
import com.maksimowiczm.zebra.core.database.model.VaultEntity

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