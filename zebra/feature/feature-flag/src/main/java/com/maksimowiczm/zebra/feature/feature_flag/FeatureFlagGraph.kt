package com.maksimowiczm.zebra.feature.feature_flag

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.maksimowiczm.zebra.core.data.model.FeatureFlag
import kotlinx.serialization.Serializable

@Serializable
data object FeatureFlagRoute

sealed interface FeatureFlagScreen {
    @Serializable
    data object FeatureFlagHomeScreen : FeatureFlagScreen
}

fun NavGraphBuilder.featureFlagGraph(
    navController: NavController,
    onShareToggle: (Boolean, fallback: (Boolean) -> Unit) -> Unit,
) {
    navigation<FeatureFlagRoute>(
        startDestination = FeatureFlagScreen.FeatureFlagHomeScreen
    ) {
        composable<FeatureFlagScreen.FeatureFlagHomeScreen> {
            val viewmodel = hiltViewModel<FeatureFlagViewModel>()

            FeatureFlagScreen(
                onNavigateUp = { navController.popBackStack() },
                viewModel = viewmodel,
                onToggle = { flag, value ->
                    when (flag) {
                        FeatureFlag.FEATURE_SHARE -> onShareToggle(value) {
                            viewmodel.updateFeatureFlag(flag, it)
                        }
                    }
                }
            )
        }
    }
}