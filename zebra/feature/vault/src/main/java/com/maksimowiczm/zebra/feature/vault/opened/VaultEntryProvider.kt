package com.maksimowiczm.zebra.feature.vault.opened

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.maksimowiczm.zebra.core.data.model.VaultEntry

class VaultEntryProvider : PreviewParameterProvider<VaultEntry> {
    override val values = VaultEntryListProvider().values.flatMap { it }
}

class VaultEntryListProvider : PreviewParameterProvider<List<VaultEntry>> {
    override val values = sequenceOf(
        emptyList(),
        listOf(
            VaultEntry(
                title = "github",
                username = "maksimowiczm",
                password = { "" },
                url = "https://github.com"
            )
        ),
        listOf(
            VaultEntry(
                title = "github",
                username = "maksimowiczm",
                password = { "" },
                url = "https://github.com"
            ),
            VaultEntry(
                title = "google",
                url = "https://google.com",
            ),
            VaultEntry(
                title = "youtube",
                username = "zebra",
                url = "https://youtube.com"
            ),
            VaultEntry(),
            // brainrot entry
            VaultEntry(
                title = "skibidi",
                password = { "toilet" }
            )
        )
    )
}