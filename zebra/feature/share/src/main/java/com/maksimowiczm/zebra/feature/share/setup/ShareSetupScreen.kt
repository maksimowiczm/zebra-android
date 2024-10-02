package com.maksimowiczm.zebra.feature.share.setup

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.zebra.feature.share.R
import com.maksimowiczm.zebra.core.common_ui.theme.ZebraTheme
import com.maksimowiczm.zebra.core.domain.SetupError

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
                error = state.error,
                isLoading = state.isLoading,
                signalingServerUrl = state.signalingServer,
                onInput = viewModel::onInput,
                onSubmit = { viewModel.onSetup(validate = true) },
                onForceSubmit = { viewModel.onSetup(validate = false) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareSetupScreen(
    onNavigateUp: () -> Unit,
    error: SetupError?,
    isLoading: Boolean,
    signalingServerUrl: String,
    onInput: (String) -> Unit,
    onSubmit: () -> Unit,
    onForceSubmit: () -> Unit,
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.share_setup),
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
                text = stringResource(R.string.you_need_to_setup_a_zebrasignal_server),
                style = MaterialTheme.typography.bodyLarge,
            )
            Form(
                error = error,
                isLoading = isLoading,
                signalingServerUrl = signalingServerUrl,
                onInput = onInput,
                onSubmit = onSubmit,
                onCancel = onNavigateUp,
                onForceSubmit = onForceSubmit,
            )
        }
    }
}

@Composable
private fun Form(
    error: SetupError?,
    isLoading: Boolean,
    signalingServerUrl: String,
    onInput: (String) -> Unit,
    onSubmit: () -> Unit,
    onForceSubmit: () -> Unit,
    onCancel: () -> Unit,
) {
    val isError = error != null

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
            when (error) {
                SetupError.SignalingChannelPingPongFailed -> {
                    Text(
                        modifier = Modifier.padding(bottom = 8.dp),
                        text = stringResource(R.string.invalid_url),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                SetupError.UnsecureConnection -> {
                    Text(
                        modifier = Modifier.padding(bottom = 8.dp),
                        text = "Unsecure connection. Only HTTPS is allowed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                null -> Unit
            }

            TextField(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                value = signalingServerUrl,
                onValueChange = onInput,
                label = { Text(stringResource(R.string.url)) },
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
            if (isError && !isLoading) {
                Column {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onSubmit,
                    ) {
                        Text(stringResource(R.string.try_again))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = onCancel,
                            colors = ButtonDefaults.buttonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = onForceSubmit,
                        ) {
                            Text(stringResource(R.string.setup_anyway))
                        }
                    }
                }
            } else {
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
                            contentDescription = stringResource(R.string.validating_url),
                        )
                    } else {
                        Text(stringResource(R.string.setup))
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ShareSetupScreenPreview(
    @PreviewParameter(SetupErrorPreviewParameterProvider::class) error: SetupError?,
) {
    ZebraTheme {
        Surface {
            ShareSetupScreen(
                onNavigateUp = {},
                isLoading = false,
                error = error,
                signalingServerUrl = "https://zebra.com",
                onInput = {},
                onSubmit = {},
                onForceSubmit = {},
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
                error = null,
                signalingServerUrl = "https://zebra.com",
                onInput = {},
                onSubmit = {},
                onForceSubmit = {},
            )
        }
    }
}

private class SetupErrorPreviewParameterProvider : PreviewParameterProvider<SetupError?> {
    override val values: Sequence<SetupError?> = sequenceOf(
        null,
        SetupError.SignalingChannelPingPongFailed,
        SetupError.UnsecureConnection,
    )
}