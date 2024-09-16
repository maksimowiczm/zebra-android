package com.maksimowiczm.zebra.feature.vault.biometrics

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.zebra.core.biometry.BiometricManager
import com.maksimowiczm.zebra.core.common_ui.composable.BooleanParameterPreviewProvider
import com.maksimowiczm.zebra.core.common_ui.theme.ZebraTheme
import com.maksimowiczm.zebra.feature_vault.R

@Composable
internal fun SetupScreen(
    viewModel: BiometricsViewModel = hiltViewModel(),
    biometricManager: BiometricManager,
    onNavigateUp: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        if (state is BiometricsUiState.Success) {
            onNavigateUp()
        }
    }

    when (state) {
        BiometricsUiState.Loading -> LoadingScreen(
            onNavigateUp = onNavigateUp,
            closable = false,
        )

        BiometricsUiState.Failed -> SetupScreen(
            onNavigateUp = onNavigateUp,
            onSetup = {
                viewModel.onSetup(
                    biometricManager = biometricManager,
                    password = it
                )
            },
            failed = true,
        )

        BiometricsUiState.Setup -> SetupScreen(
            onNavigateUp = onNavigateUp,
            onSetup = {
                viewModel.onSetup(
                    biometricManager = biometricManager,
                    password = it
                )
            },
            failed = false,
        )

        BiometricsUiState.Success -> LoadingScreen(
            onNavigateUp = onNavigateUp,
            closable = false,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadingScreen(
    onNavigateUp: () -> Unit,
    closable: Boolean,
) {
    if (!closable) {
        BackHandler {}
    }

    Column {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.setup_biometrics),
                    style = MaterialTheme.typography.headlineLarge,
                )
            },
            actions = {
                if (closable) {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                }
            }
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
private fun SetupScreen(
    onNavigateUp: () -> Unit,
    onSetup: (String) -> Unit,
    failed: Boolean,
) {
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
                    text = stringResource(R.string.setup_biometrics),
                    style = MaterialTheme.typography.headlineLarge,
                )
            },
            actions = {}
        )
        Column(
            modifier = Modifier
                .padding(16.dp)
                .imePadding()
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.setup_biometrics_description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Justify,
                )
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Box(
                            modifier = Modifier.height(32.dp)
                        ) {
                            if (failed) {
                                Text(
                                    text = stringResource(R.string.invalid_credentials),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Justify,
                                )
                            }
                        }
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            isError = failed,
                            singleLine = true,
                            value = password,
                            onValueChange = { password = it },
                            label = {
                                Text(stringResource(R.string.password))
                            },
                            keyboardActions = KeyboardActions(onDone = { onSetup(password) }),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = visualTransformation,
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        painter = painterResource(icon),
                                        contentDescription = description
                                    )
                                }
                            }
                        )
                    }
                }
            }

            Column {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSetup(password) },
                ) {
                    Text(stringResource(R.string.setup))
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateUp
                ) {
                    Text(stringResource(R.string.cancel))
                }
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
                onNavigateUp = {},
                closable = true
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SetupScreenPreview(
    @PreviewParameter(BooleanParameterPreviewProvider::class) failed: Boolean,
) {
    ZebraTheme {
        Surface {
            SetupScreen(
                onSetup = {},
                onNavigateUp = {},
                failed = failed,
            )
        }
    }
}