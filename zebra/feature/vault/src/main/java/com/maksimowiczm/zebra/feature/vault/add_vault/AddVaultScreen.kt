package com.maksimowiczm.zebra.feature.vault.add_vault

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.zebra.core.common_ui.theme.ZebraTheme
import com.maksimowiczm.zebra.feature_vault.R
import kotlinx.coroutines.delay

@Composable
internal fun AddVaultScreen(
    viewModel: AddVaultViewModel,
    onNavigateUp: () -> Unit,
) {
    var pickerLaunched by rememberSaveable { mutableStateOf(false) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        if (it == null) {
            viewModel.onCanceled()
            pickerLaunched = false
            return@rememberLauncherForActivityResult
        }

        viewModel.onFilePicked(it)
        pickerLaunched = false
    }

    var justLaunched by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(justLaunched) {
        viewModel.onStart()
        justLaunched = false
    }

    LaunchedEffect(state) {
        if (state is AddVaultUiState.PickFile && !pickerLaunched) {
            filePickerLauncher.launch("*/*")
            pickerLaunched = true
        }
    }

    when (state) {
        AddVaultUiState.Idle,
        AddVaultUiState.Loading -> LoadingScreen(onNavigateUp = onNavigateUp)

        AddVaultUiState.PickFile -> {}

        AddVaultUiState.PickFileCanceled -> CancelScreen(
            onRetry = {
                viewModel.onRetry()
            },
            onNavigateUp = onNavigateUp
        )

        is AddVaultUiState.FileReady -> FileReadyScreen(
            name = (state as AddVaultUiState.FileReady).name,
            onNavigateUp = onNavigateUp,
            onNameChange = { viewModel.onNameChanged(it) },
            onAdd = {
                viewModel.onAdd()
            }
        )

        AddVaultUiState.Done -> DoneScreen(onNavigateUp = onNavigateUp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onNavigateUp: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.add_vault),
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
    onNavigateUp: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(onNavigateUp = onNavigateUp)
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
        TopBar(onNavigateUp = onNavigateUp)
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
            onNavigateUp = onNavigateUp
        )
        FileReadyContent(
            name = name,
            onNameChange = onNameChange,
            onAdd = onAdd,
        )
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        FloatingActionButton(
            modifier = Modifier.padding(16.dp),
            onClick = onAdd,
        ) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun DoneScreen(onNavigateUp: () -> Unit) {
    var progress by rememberSaveable { mutableFloatStateOf(0f) }

    if (progress < 300f) {
        LaunchedEffect(progress) {
            delay(10)
            progress += 1
        }
    } else {
        onNavigateUp()
    }


    Column {
        TopBar(
            onNavigateUp = onNavigateUp
        )
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            progress = { progress / 300f },
            drawStopIndicator = {}
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onNavigateUp() },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = null,
                Modifier.size(100.dp)
            )
        }
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
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
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
            keyboardActions = KeyboardActions(onDone = { onAdd() })
        )
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
private fun DoneScreenPreview() {
    ZebraTheme {
        Surface {
            DoneScreen(
                onNavigateUp = {}
            )
        }
    }
}