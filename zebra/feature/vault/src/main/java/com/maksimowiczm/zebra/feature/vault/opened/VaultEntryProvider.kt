package com.maksimowiczm.zebra.feature.vault.opened

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.maksimowiczm.zebra.core.data.model.VaultEntry
import com.maksimowiczm.zebra.core.data.model.VaultEntryIdentifier
import java.util.UUID

class VaultEntryProvider : PreviewParameterProvider<VaultEntry> {
    override val values = VaultEntryListProvider().values.flatMap { it }
}

class VaultEntryListProvider : PreviewParameterProvider<List<VaultEntry>> {
    override val values = sequenceOf(
        emptyList(),
        listOf(
            entry(
                title = "github",
                username = "maksimowiczm",
                password = "",
                url = "https://github.com",
            )
        ),
        listOf(
            entry(
                title = "github",
                username = "maksimowiczm",
                password = "",
                url = "https://github.com",
            ),
            entry(
                title = "google",
                url = "https://google.com",
            ),
            entry(
                title = "youtube",
                username = "zebra",
                url = "https://youtube.com",
            ),
            entry(),
            // brainrot entry
            entry(
                title = "skibidi",
                password = "toilet",
            )
        )
    )
}

private fun entry(
    identifier: VaultEntryIdentifier = UUID.randomUUID(),
    title: String = "<untitled>",
    username: String? = null,
    password: String? = null,
    url: String? = null,
): VaultEntry {
    return VaultEntry(
        identifier = identifier,
        title = title,
        username = username,
        password = if (password != null) {
            { password }
        } else {
            null
        },
        url = url,
    )
}