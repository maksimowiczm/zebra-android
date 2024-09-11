package com.maksimowiczm.zebra.feature.vault.home

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.core.net.toUri
import com.maksimowiczm.zebra.core.data.model.Vault

internal class VaultListProvider : PreviewParameterProvider<List<Vault>> {
    override val values = sequenceOf(
        emptyList(),
        listOf(
            Vault(
                identifier = 0,
                name = "My vault",
                path = "/path/to/my/vault".toUri(),
                pathBroken = false,
            ),
            Vault(
                identifier = 1,
                name = "Work vault",
                path = "/path/to/work/vault".toUri(),
                pathBroken = true,
            )
        ),
        (1..100L).map {
            Vault(
                identifier = it,
                name = "Vault $it",
                path = "/path/to/vault/$it".toUri(),
                pathBroken = it % 4L == 3L,
            )
        }
    )
}