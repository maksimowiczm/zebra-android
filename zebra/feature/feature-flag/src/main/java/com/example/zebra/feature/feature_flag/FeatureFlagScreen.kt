package com.example.zebra.feature.feature_flag

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maksimowiczm.zebra.core.common_ui.theme.ZebraTheme
import com.maksimowiczm.zebra.core.data.model.FEATURE_FLAGS
import com.maksimowiczm.zebra.core.data.model.FeatureFlag

@Composable
internal fun FeatureFlagScreen(
    viewModel: FeatureFlagViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    onToggle: (FeatureFlag, Boolean) -> Unit = viewModel::updateFeatureFlag,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FeatureFlagScreen(
        onNavigateUp = onNavigateUp,
        featureFlags = uiState.featureFlags,
        onToggle = onToggle,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeatureFlagScreen(
    onNavigateUp: () -> Unit,
    featureFlags: Map<FeatureFlag, Boolean>,
    onToggle: (FeatureFlag, Boolean) -> Unit,
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.feature_flags),
                    style = MaterialTheme.typography.headlineLarge,
                )
            },
            actions = {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                }
            }
        )
        Row {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.experimental_features_may_break_things),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        LazyColumn {
            items(items = featureFlags.entries.toList(), key = { it.key }) { (featureFlag, value) ->
                Row(
                    modifier = Modifier
                        .clickable { onToggle(featureFlag, !value) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = featureFlag.title,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = value,
                        onCheckedChange = { onToggle(featureFlag, it) }
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    ZebraTheme {
        Surface {
            FeatureFlagScreen(
                onNavigateUp = {},
                featureFlags = FEATURE_FLAGS.associateWith { false },
                onToggle = { _, _ -> }
            )
        }
    }
}