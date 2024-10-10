package com.maksimowiczm.zebra.feature.vault.home

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.maksimowiczm.zebra.core.data.api.model.Vault
import com.maksimowiczm.zebra.core.data.api.model.VaultBiometricsStatus
import java.net.URI

private fun String.toURI() = URI.create(this)

internal class VaultListProvider : PreviewParameterProvider<List<Vault>> {
    override val values = sequenceOf(
        emptyList(),
        listOf(
            Vault(
                identifier = 0,
                name = "My vault",
                path = "/path/to/my/vault".toURI(),
                pathBroken = false,
                biometricsStatus = VaultBiometricsStatus.NotSet,
            ),
            Vault(
                identifier = 1,
                name = "Work vault",
                path = "/path/to/work/vault".toURI(),
                pathBroken = false,
                biometricsStatus = VaultBiometricsStatus.Enabled,
            ),
            Vault(
                identifier = 2,
                name = "Skibidi vault",
                path = "/path/to/skibidi/vault".toURI(),
                pathBroken = true,
                biometricsStatus = VaultBiometricsStatus.Enabled,
            ),
            Vault(
                identifier = 3,
                name = "Brainrot vault",
                path = "/path/to/brainrot/vault".toURI(),
                pathBroken = false,
                biometricsStatus = VaultBiometricsStatus.Broken,
            ),
        ),
        (1..100L).map {
            Vault(
                identifier = it,
                name = "Vault $it",
                path = "/path/to/vault/$it".toURI(),
                pathBroken = it % 4L == 3L,
                biometricsStatus = when (it % 3L) {
                    0L -> VaultBiometricsStatus.NotSet
                    1L -> VaultBiometricsStatus.Enabled
                    2L -> VaultBiometricsStatus.Broken
                    else -> throw IllegalStateException()
                }
            )
        }
    )
}