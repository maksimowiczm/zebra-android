package com.maksimowiczm.zebra.feature.vault.opened

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.zebra.core.common_ui.SomethingWentWrongScreen
import com.maksimowiczm.zebra.core.common_ui.theme.ZebraTheme
import com.maksimowiczm.zebra.core.data.api.model.Vault
import com.maksimowiczm.zebra.core.data.api.model.VaultEntry
import com.maksimowiczm.zebra.core.data.api.model.VaultEntryIdentifier
import com.maksimowiczm.zebra.core.data.api.model.VaultIdentifier
import com.maksimowiczm.zebra.core.network.NetworkStatus
import com.maksimowiczm.zebra.feature_vault.R
import java.net.URI

@Composable
internal fun OpenedVaultScreen(
    onNavigateUp: () -> Unit,
    onClose: () -> Unit,
    onShare: (VaultIdentifier, VaultEntryIdentifier) -> Unit,
    viewModel: OpenedVaultViewModel = hiltViewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val featureShare by viewModel.featureShare.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler {
        onNavigateUp()
    }

    LaunchedEffect(uiState) {
        if (uiState is OpenVaultUiState.Closed) {
            Toast.makeText(context, context.getString(R.string.locked), Toast.LENGTH_SHORT).show()
            onClose()
        }

        if (uiState is OpenVaultUiState.Lost) {
            onNavigateUp()
        }
    }

    when (uiState) {
        OpenVaultUiState.Loading,
        OpenVaultUiState.Closed,
            -> LoadingScreen()

        OpenVaultUiState.Lost -> SomethingWentWrongScreen(onNavigateUp)

        is OpenVaultUiState.Unlocked -> {
            val state = uiState as OpenVaultUiState.Unlocked

            val shareHandler = if (featureShare) {
                if (state.networkStatus == NetworkStatus.Online) {
                    ShareHandler.Enabled { entryIdentifier ->
                        onShare(state.vault.identifier, entryIdentifier)
                    }
                } else {
                    ShareHandler.NotAvailable
                }
            } else {
                ShareHandler.Disabled
            }

            OpenedVaultScreen(
                vault = state.vault,
                onNavigateUp = onNavigateUp,
                entries = state.entries,
                onLock = { viewModel.onLock() },
                onCopy = { text, hide -> viewModel.onCopy(text, hide) },
                shareHandler = shareHandler
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OpenedVaultScreen(
    onNavigateUp: () -> Unit,
    onLock: () -> Unit,
    onCopy: (String, Boolean) -> Unit,
    shareHandler: ShareHandler,
    vault: Vault,
    entries: List<VaultEntry>,
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    text = vault.name,
                    style = MaterialTheme.typography.headlineLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            actions = {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    state = rememberTooltipState(),
                    tooltip = {
                        Text(stringResource(R.string.lock))
                    }
                ) {
                    IconButton(
                        onClick = onLock
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = stringResource(R.string.lock)
                        )
                    }
                }
                IconButton(
                    onClick = onNavigateUp
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close)
                    )
                }
            }
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn {
                items(entries) {
                    VaultEntryListItem(
                        entry = it,
                        onCopy = onCopy,
                        shareHandler = shareHandler,
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}


@PreviewLightDark
@Composable
private fun OpenedVaultScreenPreview(
    @PreviewParameter(VaultEntryListProvider::class) entries: List<VaultEntry>,
) {
    ZebraTheme {
        Surface {
            OpenedVaultScreen(
                onNavigateUp = {},
                onLock = {},
                onCopy = { _, _ -> },
                vault = Vault(
                    identifier = 0,
                    name = "My vault",
                    path = URI.create("")
                ),
                entries = entries,
                shareHandler = ShareHandler.Disabled
            )
        }
    }
}