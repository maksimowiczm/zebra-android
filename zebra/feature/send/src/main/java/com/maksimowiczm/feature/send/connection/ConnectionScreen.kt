package com.maksimowiczm.feature.send.connection

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.feature.send.R
import com.maksimowiczm.zebra.core.common_ui.theme.ZebraTheme

@Composable
internal fun ConnectionScreen(
    viewmodel: ConnectionViewModel = hiltViewModel(),
    onSuccessBack: () -> Unit,
    onFailureBack: () -> Unit,
) {
    val uiState by viewmodel.state.collectAsStateWithLifecycle()

    BackHandler {
        when (uiState) {
            ConnectionUiState.Done,
            ConnectionUiState.Loading,
            ConnectionUiState.VaultLocked,
            -> onSuccessBack()

            ConnectionUiState.Failed,
            ConnectionUiState.Timeout,
            -> onFailureBack()
        }

        viewmodel.onCancel()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            ConnectionUiState.VaultLocked -> onSuccessBack()
            ConnectionUiState.Done, ConnectionUiState.Failed, ConnectionUiState.Loading, ConnectionUiState.Timeout -> {}
        }
    }

    when (uiState) {
        ConnectionUiState.Done -> DoneScreen(onSuccessBack)
        ConnectionUiState.Failed,
        ConnectionUiState.Timeout,
        -> FailureScreen(
            text = uiState.toString(),
            onTryAgain = {
                onFailureBack()
            },
            onCancel = {
                viewmodel.onCancel()
                onSuccessBack()
            }
        )

        ConnectionUiState.Loading,
        ConnectionUiState.VaultLocked,
        -> ConnectionScreen(
            text = uiState.toString(),
            onCancel = {
                viewmodel.onCancel()
                onSuccessBack()
            }
        )
    }

}

@Composable
private fun ConnectionScreen(
    text: String,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(.5f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineLarge,
            )
        }
        Column(
            modifier = Modifier
                .weight(.5f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = onCancel
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun DoneScreen(
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(.5f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                text = stringResource(R.string.done),
                style = MaterialTheme.typography.headlineLarge,
            )
        }
        Column(
            modifier = Modifier
                .weight(.5f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = onDone
            ) {
                Text(
                    text = stringResource(R.string.ok),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun FailureScreen(
    text: String,
    onTryAgain: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(.5f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineLarge,
            )
        }
        Column(
            modifier = Modifier
                .weight(.5f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = onTryAgain
            ) {
                Text(
                    text = stringResource(R.string.try_again),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = onCancel
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun DoneScreenPreview() {
    ZebraTheme {
        Surface {
            DoneScreen {}
        }
    }
}

@PreviewLightDark
@Composable
private fun ConnectionScreenPreview() {
    ZebraTheme {
        Surface {
            ConnectionScreen("Loading...") {}
        }
    }
}

@PreviewLightDark
@Composable
private fun FailureScreenPreview() {
    ZebraTheme {
        Surface {
            FailureScreen(
                text = "Failed",
                onTryAgain = {},
                onCancel = {}
            )
        }
    }
}