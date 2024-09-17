package com.maksimowiczm.zebra.feature.vault.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.zebra.core.common_ui.theme.ZebraTheme
import com.maksimowiczm.zebra.core.data.model.Vault
import com.maksimowiczm.zebra.core.data.model.VaultBiometricsStatus
import com.maksimowiczm.zebra.feature_vault.R

@Composable
internal fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onVaultClick: (Vault) -> Unit,
    onImport: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Loading()
    } else {
        HomeScreen(
            vaults = state.vaults,
            onImport = onImport,
            onDelete = { viewModel.onDelete(it) },
            onVaultClick = onVaultClick,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Loading() {
    Column {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.vaults),
                    style = MaterialTheme.typography.headlineLarge
                )
            },
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun HomeScreen(
    vaults: List<Vault>,
    onImport: () -> Unit,
    onDelete: (Vault) -> Unit,
    onVaultClick: (Vault) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopBar(
            onAdd = onImport
        )
        Content(
            vaults = vaults,
            onImport = onImport,
            onDelete = onDelete,
            onClick = onVaultClick,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onAdd: () -> Unit,
) {
    TopAppBar(
        actions = {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                tooltip = {
                    RichTooltip {
                        Text(
                            text = stringResource(R.string.import_vault)
                        )
                    }
                },
                state = rememberTooltipState(),
            ) {
                IconButton(
                    onClick = onAdd,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.import_vault),
                    )
                }
            }
        },
        title = {
            Text(
                text = stringResource(R.string.vaults),
                style = MaterialTheme.typography.headlineLarge
            )
        },
    )
}

@Composable
private fun Content(
    vaults: List<Vault>,
    onClick: (Vault) -> Unit,
    onImport: () -> Unit,
    onDelete: (Vault) -> Unit,
) {
    if (vaults.isEmpty()) {
        Empty(
            onImport = onImport
        )
    } else {
        VaultList(
            vaults = vaults,
            onClick = onClick,
            onDelete = onDelete,
        )
    }
}

@Composable
fun Empty(
    onImport: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.seems_like_you_don_t_have_any_vaults_yet),
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(
            modifier = Modifier.padding(16.dp),
            onClick = onImport
        ) {
            Text(
                text = stringResource(R.string.import_vault),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun VaultList(
    vaults: List<Vault>,
    onClick: (Vault) -> Unit,
    onDelete: (Vault) -> Unit,
) {
    LazyColumn {
        items(
            items = vaults,
            key = { it.name }
        ) { vault ->
            if (vault.pathBroken) {
                BrokenVaultListItem(
                    name = vault.name,
                    onDelete = { onDelete(vault) },
                )
            } else {
                VaultListItem(
                    vault = vault,
                    onClick = { onClick(vault) }
                )
            }
        }
    }
}

@Composable
private fun VaultListItem(
    vault: Vault,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = {
            Text(
                text = vault.name,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        trailingContent = {
            when (vault.biometricsStatus) {
                VaultBiometricsStatus.Broken -> {
                    Icon(
                        painter = painterResource(R.drawable.ic_fingerprint_off),
                        contentDescription = "Vault has biometrics."
                    )
                }

                VaultBiometricsStatus.Enabled -> {
                    Icon(
                        painter = painterResource(R.drawable.ic_fingerprint),
                        contentDescription = "Vault has biometrics."
                    )
                }

                VaultBiometricsStatus.NotSet -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.open_vault),
                    )
                }
            }
        },
    )
}

@Composable
private fun BrokenVaultListItem(
    name: String,
    onDelete: () -> Unit,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    if (showDialog) {
        BrokenVaultDialog(
            onDismiss = { showDialog = false },
            onDelete = onDelete,
        )
    }

    ListItem(
        modifier = Modifier.clickable { showDialog = true },
        headlineContent = {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        trailingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_forgor),
                contentDescription = "Vault broken.",
            )
        },
    )
}

@Composable
private fun BrokenVaultDialog(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            OutlinedButton(
                onClick = onDelete
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        title = { Text(stringResource(R.string.vault_broken)) },
        text = { Text(stringResource(R.string.vault_is_broken_do_you_want_to_delete_it)) }
    )
}


@PreviewLightDark
@Composable
private fun HomeScreenLoadingPreview() {
    ZebraTheme {
        Surface {
            Loading()
        }
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenPreview(
    @PreviewParameter(VaultListProvider::class) vaults: List<Vault>,
) {
    ZebraTheme {
        Surface {
            HomeScreen(
                vaults = vaults,
                onImport = {},
                onDelete = {},
                onVaultClick = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun BrokenVaultDialogPreview() {
    ZebraTheme {
        BrokenVaultDialog(
            onDismiss = {},
            onDelete = {},
        )
    }
}