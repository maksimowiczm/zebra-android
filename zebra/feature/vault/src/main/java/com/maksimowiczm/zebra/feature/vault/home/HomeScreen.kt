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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.zebra.core.common_ui.theme.ZebraTheme
import com.maksimowiczm.zebra.core.data.model.Vault
import com.maksimowiczm.zebra.feature_vault.R

@Composable
internal fun HomeScreen(
    viewModel: HomeViewModel,
    onAdd: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var justLaunched by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(justLaunched) {
        viewModel.onStart()
        justLaunched = false
    }

    if (state.isLoading) {
        Loading()
    } else {
        HomeScreen(
            vaults = state.vaults,
            onAdd = onAdd,
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
    onAdd: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopBar(
            onAdd = onAdd
        )
        Content(
            vaults = vaults,
            onAdd = onAdd,
            onClick = {}
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
                            text = stringResource(R.string.add_vault)
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
                        contentDescription = stringResource(R.string.add_vault),
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
    onAdd: () -> Unit,
) {
    if (vaults.isEmpty()) {
        Empty(
            onAdd = onAdd
        )
    } else {
        VaultList(
            vaults = vaults,
            onClick = onClick
        )
    }
}

@Composable
fun Empty(
    onAdd: () -> Unit
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
            onClick = onAdd
        ) {
            Text(
                text = stringResource(R.string.add_vault),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun VaultList(
    vaults: List<Vault>,
    onClick: (Vault) -> Unit,
) {
    LazyColumn {
        items(vaults) { vault ->
            ListItem(
                modifier = Modifier.clickable { onClick(vault) },
                headlineContent = {
                    Text(
                        text = vault.name,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.open_vault),
                    )
                },
            )
        }
    }
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
                onAdd = {}
            )
        }
    }
}