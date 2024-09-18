package com.maksimowiczm.zebra.feature.vault.unlock

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.zebra.core.biometry.BiometricManager
import com.maksimowiczm.zebra.core.biometry.BiometryStatus
import com.maksimowiczm.zebra.core.common_ui.SomethingWentWrongScreen
import com.maksimowiczm.zebra.core.common_ui.theme.ZebraTheme
import com.maksimowiczm.zebra.core.data.model.VaultBiometricsStatus
import com.maksimowiczm.zebra.feature_vault.R

@Composable
internal fun UnlockVaultScreen(
    viewModel: UnlockVaultViewModel = hiltViewModel(),
    biometricManager: BiometricManager,
    onNavigateUp: () -> Unit,
    onUnlocked: () -> Unit,
    onBiometricsSetup: () -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    BackHandler {
        onNavigateUp()
    }

    var hasPrompted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(hasPrompted) {
        if (!hasPrompted) {
            viewModel.tryUnlock(biometricManager)
            hasPrompted = true
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is UnlockUiState.Unlocked) {
            onUnlocked()
        }
    }

    when (val state = uiState) {
        UnlockUiState.Loading -> LoadingScreen(
            title = stringResource(R.string.unlock),
            onClose = onNavigateUp
        )

        is UnlockUiState.ReadyToUnlock -> {
            if (state.credentialsFailed) {
                UnlockVaultScreen(
                    state = UnlockVaultScreenState.Failed(state.biometricsStatus),
                    onUnlock = { viewModel.onUnlock(it) },
                    onBiometricsUnlock = { viewModel.tryUnlock(biometricManager) },
                    onBiometricsSetup = { onBiometricsSetup() },
                    onBiometricsInvalidatedAcknowledge = { viewModel.onBiometricsInvalidatedAcknowledge() },
                    hasBiometrics = biometricManager.hasBiometric() is BiometryStatus.Ok,
                )
                return
            }

            UnlockVaultScreen(
                state = UnlockVaultScreenState.Idle(state.biometricsStatus),
                onUnlock = { viewModel.onUnlock(it) },
                onBiometricsUnlock = { viewModel.tryUnlock(biometricManager) },
                onBiometricsSetup = { onBiometricsSetup() },
                onBiometricsInvalidatedAcknowledge = { viewModel.onBiometricsInvalidatedAcknowledge() },
                hasBiometrics = biometricManager.hasBiometric() is BiometryStatus.Ok,
            )
        }

        UnlockUiState.Unlocked -> LoadingScreen(
            title = stringResource(R.string.unlocked),
            onClose = onNavigateUp
        )

        is UnlockUiState.Unlocking -> UnlockVaultScreen(
            state = UnlockVaultScreenState.Unlocking(state.biometricsStatus),
            onUnlock = {},
            onBiometricsUnlock = {},
            onBiometricsSetup = {},
            onBiometricsInvalidatedAcknowledge = { viewModel.onBiometricsInvalidatedAcknowledge() },
            hasBiometrics = biometricManager.hasBiometric() is BiometryStatus.Ok,
        )

        UnlockUiState.UnrecoverableError -> SomethingWentWrongScreen(onNavigateUp = onNavigateUp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadingScreen(
    title: String,
    onClose: () -> Unit,
) {
    Column {
        TopAppBar(
            title = { Text(text = title, style = MaterialTheme.typography.headlineLarge) },
            actions = {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close)
                    )
                }
            })
        Column(
            Modifier
                .fillMaxSize()
                .wrapContentSize()
        ) {
            CircularProgressIndicator()
        }
    }
}

private sealed interface UnlockVaultScreenState {
    val biometricsStatus: VaultBiometricsStatus

    data class Idle(
        override val biometricsStatus: VaultBiometricsStatus,
    ) : UnlockVaultScreenState

    data class Unlocking(
        override val biometricsStatus: VaultBiometricsStatus,
    ) : UnlockVaultScreenState

    data class Failed(
        override val biometricsStatus: VaultBiometricsStatus,
    ) : UnlockVaultScreenState
}

@Composable
private fun BrokenBiometryDialog(
    onAcknowledge: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            // don't allow dismissing. Important info
        },
        title = {
            Text(
                text = stringResource(R.string.biometrics_invalidated),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.seems_like_you_enrolled_new_biometrics),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(onClick = onAcknowledge) {
                Text(text = stringResource(R.string.ok))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnlockVaultScreen(
    state: UnlockVaultScreenState,
    onUnlock: (String) -> Unit,
    hasBiometrics: Boolean,
    onBiometricsSetup: () -> Unit,
    onBiometricsUnlock: () -> Unit,
    onBiometricsInvalidatedAcknowledge: () -> Unit,
) {
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    if (state.biometricsStatus is VaultBiometricsStatus.Broken) {
        BrokenBiometryDialog(onAcknowledge = onBiometricsInvalidatedAcknowledge)
    }

    Column(
        modifier = Modifier.imePadding()
    ) {
        TopAppBar(title = {
            Text(
                text = stringResource(R.string.unlock),
                style = MaterialTheme.typography.headlineLarge,
            )
        })
        Column(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier.weight(.5f),
                verticalArrangement = Arrangement.Bottom,
            ) {
                when (state) {
                    is UnlockVaultScreenState.Failed -> Text(
                        text = stringResource(R.string.invalid_credentials),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                    )

                    is UnlockVaultScreenState.Idle -> Text(
                        text = stringResource(R.string.enter_password),
                        style = MaterialTheme.typography.titleMedium,
                    )

                    is UnlockVaultScreenState.Unlocking -> {}
                }
            }
            Column(
                modifier = Modifier.weight(.5f),
                verticalArrangement = Arrangement.Top,
            ) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state !is UnlockVaultScreenState.Unlocking,
                    isError = state is UnlockVaultScreenState.Failed,
                    singleLine = true,
                    value = if (state is UnlockVaultScreenState.Unlocking) stringResource(R.string.unlocking) else password,
                    onValueChange = { password = it },
                    visualTransformation = if (passwordVisible || state is UnlockVaultScreenState.Unlocking) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    keyboardActions = KeyboardActions(onDone = { onUnlock(password) }),
                    trailingIcon = {
                        val painter = if (passwordVisible) {
                            painterResource(R.drawable.ic_visibility_off)
                        } else {
                            painterResource(R.drawable.ic_visibility)
                        }

                        val description = if (passwordVisible) {
                            stringResource(R.string.show)
                        } else {
                            stringResource(R.string.hide)
                        }

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painter,
                                contentDescription = description
                            )
                        }
                    },
                )
            }

            if (hasBiometrics) {
                if (state.biometricsStatus is VaultBiometricsStatus.Enabled) {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state !is UnlockVaultScreenState.Unlocking,
                        onClick = onBiometricsUnlock,
                    ) {
                        Text(
                            text = stringResource(R.string.use_biometrics),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                } else if (state.biometricsStatus is VaultBiometricsStatus.NotSet) {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state !is UnlockVaultScreenState.Unlocking,
                        onClick = onBiometricsSetup,
                    ) {
                        Text(
                            text = stringResource(R.string.setup_biometrics),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is UnlockVaultScreenState.Unlocking,
                onClick = { onUnlock(password) },
            ) {
                Text(
                    text = stringResource(R.string.unlock),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

private class UnlockVaultScreenStatePreviewProvider :
    PreviewParameterProvider<UnlockVaultScreenState> {

    val biometricsStatus = sequenceOf(
        VaultBiometricsStatus.NotSet,
        VaultBiometricsStatus.Enabled,
        VaultBiometricsStatus.Broken
    )

    val notSet = biometricsStatus.map { UnlockVaultScreenState.Idle(it) }
    val failed = biometricsStatus.map { UnlockVaultScreenState.Failed(it) }
    val unlocking = biometricsStatus.map { UnlockVaultScreenState.Unlocking(it) }

    override val values = notSet + failed + unlocking
}

@PreviewLightDark
@Composable
private fun BiometricsUnlockVaultScreenPreview(
    @PreviewParameter(UnlockVaultScreenStatePreviewProvider::class) state: UnlockVaultScreenState,
) {
    ZebraTheme {
        Surface {
            UnlockVaultScreen(
                state = state,
                onUnlock = {},
                onBiometricsUnlock = {},
                onBiometricsSetup = {},
                onBiometricsInvalidatedAcknowledge = {},
                hasBiometrics = true,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun UnlockVaultScreenPreview(
    @PreviewParameter(UnlockVaultScreenStatePreviewProvider::class) state: UnlockVaultScreenState,
) {
    ZebraTheme {
        Surface {
            UnlockVaultScreen(
                state = state,
                onUnlock = {},
                onBiometricsUnlock = {},
                onBiometricsSetup = {},
                onBiometricsInvalidatedAcknowledge = {},
                hasBiometrics = false,
            )
        }
    }
}