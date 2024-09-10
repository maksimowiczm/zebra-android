package com.maksimowiczm.zebra.feature.vault.home

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.maksimowiczm.zebra.core.data.api.model.Vault

internal class VaultListProvider : PreviewParameterProvider<List<Vault>> {
    override val values = sequenceOf(
        emptyList(),
        listOf(
            Vault(
                name = "My vault",
            ),
            Vault(
                name = "Work vault",
            )
        ),
        (1..100).map {
            Vault(
                name = "Vault $it"
            )
        }
    )
}