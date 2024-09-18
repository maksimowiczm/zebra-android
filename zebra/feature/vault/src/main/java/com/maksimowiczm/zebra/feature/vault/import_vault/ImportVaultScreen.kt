package com.maksimowiczm.zebra.feature.vault.import_vault

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.zebra.core.biometry.BiometricManager
import com.maksimowiczm.zebra.core.biometry.BiometryStatus
import com.maksimowiczm.zebra.core.common_ui.SomethingWentWrongScreen
import com.maksimowiczm.zebra.core.common_ui.composable.BooleanParameterPreviewProvider
import com.maksimowiczm.zebra.core.common_ui.theme.ZebraTheme
import com.maksimowiczm.zebra.core.data.model.Vault
import com.maksimowiczm.zebra.feature.vault.home.VaultListProvider
import com.maksimowiczm.zebra.feature_vault.R

@Composable
internal fun ImportVaultScreen(
    viewModel: ImportVaultViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    onVaultExists: (Vault) -> Unit,
    biometricManager: BiometricManager,
    onBiometricsSetup: (Vault) -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    var pickerLaunched by rememberSaveable { mutableStateOf(false) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) {
        if (it == null) {
            viewModel.onCanceled()
            pickerLaunched = false
            return@rememberLauncherForActivityResult
        }

        viewModel.onFilePicked(it)
        pickerLaunched = false
    }

    LaunchedEffect(uiState) {
        if (uiState is ImportVaultUiState.PickFile && !pickerLaunched) {
            filePickerLauncher.launch(arrayOf("*/*"))
            pickerLaunched = true
        }
        if (uiState is ImportVaultUiState.VaultExists) {
            onVaultExists((uiState as ImportVaultUiState.VaultExists).vault)
        }
    }

    when (uiState) {
        ImportVaultUiState.PickFile,
        ImportVaultUiState.Idle,
        ImportVaultUiState.Loading,
        is ImportVaultUiState.VaultExists,
        -> LoadingScreen(onNavigateUp = onNavigateUp)

        ImportVaultUiState.PickFileCanceled -> CancelScreen(
            onRetry = {
                viewModel.onRetry()
            },
            onNavigateUp = onNavigateUp
        )

        is ImportVaultUiState.FileReady -> {
            val state = uiState as ImportVaultUiState.FileReady
            FileReadyScreen(
                name = state.name,
                onNavigateUp = onNavigateUp,
                onNameChange = { viewModel.onNameChanged(it) },
                onAdd = { viewModel.onImport() },
            )
        }

        is ImportVaultUiState.Done -> DoneScreen(
            onNavigateUp = onNavigateUp,
            hasBiometrics = biometricManager.hasBiometric() == BiometryStatus.Ok,
            onBiometricsSetup = onBiometricsSetup,
            vault = (uiState as ImportVaultUiState.Done).vault
        )

        ImportVaultUiState.FileImportError -> SomethingWentWrongScreen(onNavigateUp = onNavigateUp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    title: String,
    onNavigateUp: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
            )
        },
        actions = {
            IconButton(
                onClick = onNavigateUp
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close),
                )
            }
        }
    )
}

@Composable
private fun LoadingScreen(
    onNavigateUp: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            title = stringResource(R.string.import_vault),
            onNavigateUp = onNavigateUp
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun CancelScreen(
    onRetry: () -> Unit,
    onNavigateUp: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            title = stringResource(R.string.import_vault),
            onNavigateUp = onNavigateUp
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.whoops),
                style = MaterialTheme.typography.titleLarge,
            )

            Column(modifier = Modifier.padding(32.dp)) {
                Text(
                    text = stringResource(R.string.seems_like_you_didn_t_pick_a_file),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(R.string.do_you_want_to_try_again),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(fraction = .5f),
            ) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRetry,
                ) {
                    Text(
                        text = stringResource(R.string.retry),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                FilledTonalButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateUp,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun FileReadyScreen(
    name: String,
    onNavigateUp: () -> Unit,
    onNameChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopBar(
            title = stringResource(R.string.import_vault),
            onNavigateUp = onNavigateUp
        )
        FileReadyContent(
            name = name,
            onNameChange = onNameChange,
            onAdd = onAdd,
        )
    }
}

@Composable
private fun FileReadyContent(
    name: String,
    onNameChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier.weight(.5f),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = stringResource(R.string.setup_vault),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Column(
            modifier = Modifier.weight(.5f),
            verticalArrangement = Arrangement.Top
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                onValueChange = onNameChange,
                singleLine = true,
                label = {
                    Text(
                        text = stringResource(R.string.name),
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                keyboardActions = KeyboardActions(onDone = { onAdd() }),
            )
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onAdd,
        ) {
            Text(
                text = stringResource(R.string.import_),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun DoneScreen(
    onNavigateUp: () -> Unit,
    hasBiometrics: Boolean,
    onBiometricsSetup: (Vault) -> Unit,
    vault: Vault,
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        TopBar(
            title = stringResource(R.string.vault_imported),
            onNavigateUp = onNavigateUp
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = null,
                Modifier.size(100.dp)
            )
        }
        if (hasBiometrics) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onBiometricsSetup(vault) }
            ) {
                Text(
                    text = stringResource(R.string.setup_biometrics),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateUp
            ) {
                Text(
                    text = stringResource(R.string.skip),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        } else {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateUp
            ) {
                Text(
                    text = stringResource(R.string.continue_),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}


@PreviewLightDark
@Composable
private fun LoadingScreenPreview() {
    ZebraTheme {
        Surface {
            LoadingScreen(
                onNavigateUp = {}
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun CancelScreenPreview() {
    ZebraTheme {
        Surface {
            CancelScreen(
                onNavigateUp = {},
                onRetry = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun FileReadyScreenPreview() {
    ZebraTheme {
        Surface {
            FileReadyScreen(
                name = "Vault name",
                onNameChange = {},
                onNavigateUp = {},
                onAdd = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun DoneScreenPreview(
    @PreviewParameter(BooleanParameterPreviewProvider::class) hasBiometrics: Boolean,
) {
    ZebraTheme {
        Surface {
            DoneScreen(
                onNavigateUp = {},
                hasBiometrics = hasBiometrics,
                onBiometricsSetup = {},
                vault = VaultListProvider().values.first { it.isNotEmpty() }.first()
            )
        }
    }
}