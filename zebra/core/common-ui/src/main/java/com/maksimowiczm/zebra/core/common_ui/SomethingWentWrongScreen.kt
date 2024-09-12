package com.maksimowiczm.zebra.core.common_ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.maksimowiczm.zebra.common_ui.R
import com.maksimowiczm.zebra.core.common_ui.theme.ZebraTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SomethingWentWrongScreen(
    onNavigateUp: () -> Unit,
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.whoops),
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
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                modifier = Modifier.size(80.dp),
                painter = painterResource(R.drawable.ic_forgor),
                contentDescription = stringResource(R.string.something_went_wrong),
            )
            Text(
                text = stringResource(R.string.something_went_wrong),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SomethingWentWrongScreenPreview() {
    ZebraTheme {
        Surface {
            SomethingWentWrongScreen(onNavigateUp = {})
        }
    }
}