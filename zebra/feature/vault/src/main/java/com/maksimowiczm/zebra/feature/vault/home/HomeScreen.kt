package com.maksimowiczm.zebra.feature.vault.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.zebra.core.common_ui.theme.ZebraTheme
import com.maksimowiczm.zebra.core.data.api.model.Vault
import com.maksimowiczm.zebra.feature_vault.R

@Composable
internal fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onVaultClick: (Vault) -> Unit,
    onImport: () -> Unit,
    onShowSecret: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        LoadingScreen()
    } else {
        HomeScreen(
            vaults = state.vaults,
            onVaultClick = onVaultClick,
            onAddVault = onImport,
            onShowSecret = onShowSecret,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadingScreen() {
    Column {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.vaults),
                    style = MaterialTheme.typography.headlineLarge,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    vaults: List<Vault>,
    onVaultClick: (Vault) -> Unit,
    onAddVault: () -> Unit,
    onShowSecret: () -> Unit,
) {
    // FAB
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End,
    ) {
        ExtendedFloatingActionButton(onClick = onAddVault) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
            )
            Text(text = stringResource(R.string.add_vault))
        }
    }

    // Content
    Column {
        TopAppBar(
            title = {
                val interactionSource = remember { MutableInteractionSource() }
                var count by rememberSaveable { mutableIntStateOf(0) }
                Text(
                    modifier = Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                    ) {
                        count++
                        if (count == 5) {
                            onShowSecret()
                            count = 0
                        }
                    },
                    text = stringResource(R.string.vaults),
                    style = MaterialTheme.typography.headlineLarge,
                )
            },
            actions = {}
        )
        if (vaults.isEmpty()) {
            EmptyList()
        } else {
            VaultList(vaults = vaults, onVaultClick = onVaultClick)
        }
    }
}

@Composable
private fun EmptyList() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(R.string.so_empty))
    }
}

@Composable
private fun VaultList(
    vaults: List<Vault>,
    onVaultClick: (Vault) -> Unit,
) {
    LazyColumn {
        items(
            items = vaults,
            key = { it.identifier },
        ) {
            ListItem(
                modifier = Modifier.clickable { onVaultClick(it) },
                headlineContent = { Text(it.name) },
            )
            HorizontalDivider()
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
                onVaultClick = {},
                onAddVault = {},
                onShowSecret = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenLoadingPreview() {
    ZebraTheme {
        Surface {
            LoadingScreen()
        }
    }
}