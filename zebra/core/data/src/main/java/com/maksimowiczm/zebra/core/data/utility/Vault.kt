package com.maksimowiczm.zebra.core.data.utility

import com.maksimowiczm.zebra.core.data.api.model.Vault
import com.maksimowiczm.zebra.core.data.api.model.VaultBiometricsStatus
import com.maksimowiczm.zebra.core.database.model.VaultEntity
import java.net.URI

internal fun VaultEntity.asVault(
    pathBroken: Boolean,
    biometricsStatus: VaultBiometricsStatus,
): Vault = Vault(
    identifier = identifier,
    name = name,
    path = URI.create(path),
    pathBroken = pathBroken,
    biometricsStatus = biometricsStatus
)