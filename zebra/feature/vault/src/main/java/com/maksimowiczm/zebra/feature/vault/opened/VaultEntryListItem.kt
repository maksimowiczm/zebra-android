package com.maksimowiczm.zebra.feature.vault.opened

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.maksimowiczm.zebra.core.common_ui.theme.ZebraTheme
import com.maksimowiczm.zebra.core.data.model.VaultEntry
import com.maksimowiczm.zebra.core.data.model.VaultEntryIdentifier
import com.maksimowiczm.zebra.feature_vault.R


@Composable
internal fun VaultEntryListItem(
    entry: VaultEntry,
    onCopy: (String, Boolean) -> Unit,
    initialOpened: Boolean = false,
    onShare: (VaultEntryIdentifier) -> Unit,
) {
    var opened by rememberSaveable { mutableStateOf(initialOpened) }
    val modifier = if (opened) {
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    } else {
        Modifier.fillMaxWidth()
    }
    Column(
        modifier = modifier
    ) {
        EntryHeader(
            title = entry.title,
            opened = opened,
            onOpen = { opened = !opened },
            onShare = { onShare(entry.identifier) }
        )
        if (opened) {
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onSurface)
            )
            EntryContent(entry = entry, onCopy = onCopy)
        }
    }
}

@Composable
private fun EntryHeader(
    title: String,
    opened: Boolean,
    onOpen: () -> Unit,
    onShare: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable { onOpen() }
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = title
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onShare) {
            Icon(
                painter = painterResource(id = R.drawable.ic_qr_code_scanner),
                contentDescription = null,
            )
        }
        if (!opened) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.open)
            )
        } else {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.close)
            )
        }
    }
}

@Composable
private fun EntryContent(
    entry: VaultEntry,
    onCopy: (String, Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (entry.username != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.username),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.username!!,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { onCopy(entry.username!!, false) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_content_copy),
                                contentDescription = stringResource(R.string.copy_username)
                            )
                        }
                    }
                }
            }
        }

        if (entry.password != null) {
            var visible by rememberSaveable { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.password),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.password!!.let {
                                if (visible) {
                                    it()
                                } else {
                                    "\u2022".repeat(12)
                                }
                            },
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (visible) {
                            IconButton(onClick = { visible = !visible }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_visibility_off),
                                    contentDescription = "Hide password"
                                )
                            }
                        } else {
                            IconButton(onClick = { visible = !visible }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_visibility),
                                    contentDescription = "Show password"
                                )
                            }
                        }
                        IconButton(onClick = { onCopy(entry.password!!(), true) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_content_copy),
                                contentDescription = stringResource(R.string.copy_password)
                            )
                        }
                    }
                }
            }
        }

        if (entry.url != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.url),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.url!!,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { onCopy(entry.url!!, false) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_content_copy),
                                contentDescription = stringResource(R.string.copy_url),
                            )
                        }
                    }
                }
            }
        }
    }
}


@PreviewLightDark
@Composable
private fun ExpandedVaultEntryListItemPreview(
    @PreviewParameter(VaultEntryProvider::class) entry: VaultEntry,
) {
    ZebraTheme {
        Surface {
            VaultEntryListItem(
                entry = entry,
                initialOpened = true,
                onCopy = { _, _ -> },
                onShare = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun VaultEntryListItemPreview() {
    ZebraTheme {
        Surface {
            VaultEntryListItem(
                entry = VaultEntryProvider().values.first(),
                initialOpened = false,
                onCopy = { _, _ -> },
                onShare = {},
            )
        }
    }
}