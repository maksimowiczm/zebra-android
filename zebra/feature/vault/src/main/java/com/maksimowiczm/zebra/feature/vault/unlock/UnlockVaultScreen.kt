package com.maksimowiczm.zebra.feature.vault.unlock

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import com.maksimowiczm.zebra.feature_vault.R

@Composable
internal fun UnlockVaultScreen(
    viewModel: UnlockVaultViewModel = hiltViewModel(),
    biometricManager: BiometricManager,
    onNavigateUp: () -> Unit,
    onOpen: () -> Unit,
    onBiometrics: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.tryUnlock(
            biometricManager = biometricManager
        )
    }

    LaunchedEffect(state) {
        if (state is VaultUiState.Unlocked) {
            onOpen()
        }
    }

    val onBiometrics = {
        if (!viewModel.onBiometrics(biometricManager)) {
            onBiometrics()
        }
    }

    val hasBiometrics = biometricManager.hasBiometric() is BiometryStatus.Ok

    when (state) {
        is VaultUiState.Unlocked, VaultUiState.Loading -> LoadingScreenPreview()

        VaultUiState.PasswordFailed -> PasswordScreen(
            onNavigateUp = onNavigateUp,
            onUnlock = { viewModel.onUnlock(it) },
            failed = true,
            unlocking = false,
            hasBiometrics = hasBiometrics,
            onUseBiometrics = onBiometrics,
        )

        is VaultUiState.VaultFound -> PasswordScreen(
            onNavigateUp = onNavigateUp,
            onUnlock = { viewModel.onUnlock(it) },
            failed = false,
            unlocking = false,
            hasBiometrics = hasBiometrics,
            onUseBiometrics = onBiometrics,
            biometricsInvalidated = (state as VaultUiState.VaultFound).biometricsInvalidated,
            onBiometricsInvalidatedAcknowledge = { viewModel.onBiometricsInvalidatedAcknowledge() },
        )

        VaultUiState.Error -> SomethingWentWrongScreen(
            onNavigateUp = onNavigateUp,
        )

        VaultUiState.Unlocking -> PasswordScreen(
            onNavigateUp = onNavigateUp,
            onUnlock = { viewModel.onUnlock(it) },
            failed = false,
            unlocking = true,
            hasBiometrics = hasBiometrics,
            onUseBiometrics = onBiometrics,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasswordScreen(
    onNavigateUp: () -> Unit,
    onUnlock: (String) -> Unit,
    failed: Boolean,
    unlocking: Boolean,
    hasBiometrics: Boolean,
    onUseBiometrics: () -> Unit,
    biometricsInvalidated: Boolean = false,
    onBiometricsInvalidatedAcknowledge: () -> Unit = {},
) {
    if (biometricsInvalidated) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = "Biometrics invalidated",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = "Biometrics have been invalidated. You have to setup them again.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(onClick = { onBiometricsInvalidatedAcknowledge() }) {
                    Text("OK")
                }
            },
        )
    }

    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val description = if (passwordVisible) {
        stringResource(R.string.hide)
    } else {
        stringResource(R.string.show)
    }

    val icon = if (passwordVisible) {
        R.drawable.ic_visibility_off
    } else {
        R.drawable.ic_visibility
    }

    val visualTransformation = if (passwordVisible) {
        VisualTransformation.None
    } else {
        PasswordVisualTransformation()
    }

    Column {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.unlock),
                    style = MaterialTheme.typography.headlineLarge,
                )
            },
            actions = {
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                    )
                }
            }
        )
        Column(
            modifier = Modifier
                .padding(16.dp)
                .imePadding()
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier.height(32.dp)
                ) {
                    if (failed) {
                        Text(
                            text = stringResource(R.string.invalid_credentials),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !unlocking,
                    isError = failed,
                    singleLine = true,
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        if (unlocking) {
                            Text(stringResource(R.string.unlocking))
                        } else {
                            Text(stringResource(R.string.password))
                        }
                    },
                    keyboardActions = KeyboardActions(onDone = { onUnlock(password) }),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = visualTransformation,
                    trailingIcon = {
                        if (!unlocking) {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(icon),
                                    contentDescription = description
                                )
                            }
                        }
                    }
                )
            }

            var degrees by remember { mutableFloatStateOf(0f) }
            val rotation = remember { Animatable(degrees) }
            LaunchedEffect(unlocking) {
                if (unlocking) {
                    rotation.animateTo(
                        targetValue = degrees + 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(3000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    ) {
                        degrees = value
                    }
                }
            }

            Column {
                if (hasBiometrics) {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !unlocking,
                        onClick = onUseBiometrics,
                    ) {
                        Text(stringResource(R.string.use_biometrics))
                    }
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !unlocking,
                    onClick = { onUnlock(password) },
                ) {
                    if (unlocking) {
                        Icon(
                            modifier = Modifier.rotate(degrees),
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                        )
                    } else {
                        Text(stringResource(R.string.unlock))
                    }
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
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}


@PreviewLightDark
@Composable
private fun UnlockingVaultScreenPreview() {
    ZebraTheme {
        Surface {
            PasswordScreen(
                onNavigateUp = {},
                onUnlock = {},
                failed = false,
                unlocking = true,
                hasBiometrics = false,
                onUseBiometrics = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PasswordScreenWithBiometricsPreview(
    @PreviewParameter(BooleanParameterPreviewProvider::class)
    failed: Boolean,
) {
    ZebraTheme {
        Surface {
            PasswordScreen(
                onNavigateUp = {},
                onUnlock = {},
                failed = failed,
                hasBiometrics = true,
                unlocking = false,
                onUseBiometrics = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PasswordScreenWithoutBiometricsPreview(
    @PreviewParameter(BooleanParameterPreviewProvider::class)
    failed: Boolean,
) {
    ZebraTheme {
        Surface {
            PasswordScreen(
                onNavigateUp = {},
                onUnlock = {},
                failed = failed,
                hasBiometrics = false,
                unlocking = false,
                onUseBiometrics = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun LoadingScreenPreview() {
    ZebraTheme {
        Surface {
            LoadingScreen()
        }
    }
}