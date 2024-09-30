package com.maksimowiczm.feature.share.setup

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.feature.share.R
import com.maksimowiczm.zebra.core.common_ui.composable.BooleanParameterPreviewProvider
import com.maksimowiczm.zebra.core.common_ui.theme.ZebraTheme

@Composable
internal fun ShareSetupScreen(
    onNavigateUp: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        when (uiState) {
            SetupUiState.Done -> onNavigateUp()

            is SetupUiState.Ready,
            SetupUiState.Loading,
            -> Unit
        }
    }

    when (uiState) {
        SetupUiState.Done,
        SetupUiState.Loading,
        -> Unit

        is SetupUiState.Ready -> {
            val state = uiState as SetupUiState.Ready

            ShareSetupScreen(
                onNavigateUp = onNavigateUp,
                isError = state.isError,
                isLoading = state.isLoading,
                signalingServerUrl = state.signalingServer,
                onInput = viewModel::onInput,
                onSubmit = viewModel::onSetup,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareSetupScreen(
    onNavigateUp: () -> Unit,
    isError: Boolean,
    isLoading: Boolean,
    signalingServerUrl: String,
    onInput: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    text = "Share Setup",
                    style = MaterialTheme.typography.headlineLarge
                )
            },
            actions = {
                IconButton(
                    onClick = onNavigateUp,
                ) {
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
                .sizeIn(minHeight = 200.dp),
        ) {
            Text(
                text = "In order to share your data, you need to setup a signaling server.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Form(
                isError = isError,
                isLoading = isLoading,
                signalingServerUrl = signalingServerUrl,
                onInput = onInput,
                onSubmit = onSubmit,
            )
        }
    }
}

@Composable
private fun Form(
    isError: Boolean,
    isLoading: Boolean,
    signalingServerUrl: String,
    onInput: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(.5f),
            verticalArrangement = Arrangement.Bottom,
        ) {
            if (isError) {
                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = "Invalid signaling server URL",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = signalingServerUrl,
                onValueChange = onInput,
                label = { Text("Signaling server URL") },
                singleLine = true,
                isError = isError,
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(.5f),
            contentAlignment = Alignment.BottomCenter,
        ) {

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSubmit,
                enabled = !isLoading,
            ) {
                if (isLoading) {
                    val rotation = remember { Animatable(0f) }

                    LaunchedEffect(Unit) {
                        rotation.animateTo(
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = 1000,
                                    easing = LinearEasing
                                ),
                            ),
                        )
                    }

                    Icon(
                        modifier = Modifier.rotate(rotation.value),
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Validating signaling server URL",
                    )
                } else {
                    Text("Setup")
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ShareSetupScreenPreview(
    @PreviewParameter(BooleanParameterPreviewProvider::class) isError: Boolean,
) {
    ZebraTheme {
        Surface {
            ShareSetupScreen(
                onNavigateUp = {},
                isLoading = false,
                isError = !isError,
                signalingServerUrl = "https://zebra.com",
                onInput = {},
                onSubmit = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun LoadingShareSetupScreenPreview() {
    ZebraTheme {
        Surface {
            ShareSetupScreen(
                onNavigateUp = {},
                isLoading = true,
                isError = false,
                signalingServerUrl = "https://zebra.com",
                onInput = {},
                onSubmit = {},
            )
        }
    }
}