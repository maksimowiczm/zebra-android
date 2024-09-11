package com.maksimowiczm.zebra.feature.vault.home

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.core.net.toUri
import com.maksimowiczm.zebra.core.data.model.Vault

internal class VaultListProvider : PreviewParameterProvider<List<Vault>> {
    override val values = sequenceOf(
        emptyList(),
        listOf(
            Vault(
                name = "My vault",
                path = "/path/to/my/vault".toUri(),
            ),
            Vault(
                name = "Work vault",
                path = "/path/to/work/vault".toUri(),
            )
        ),
        (1..100).map {
            Vault(
                name = "Vault $it",
                path = "/path/to/vault/$it".toUri(),
            )
        }
    )
}