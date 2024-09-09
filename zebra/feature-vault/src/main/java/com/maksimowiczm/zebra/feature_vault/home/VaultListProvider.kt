package com.maksimowiczm.zebra.feature_vault.home

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.maksimowiczm.zebra.domain.api.model.Vault

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