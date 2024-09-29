package com.example.zebra.feature.feature_flag

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable

@Serializable
data object FeatureFlagRoute

sealed interface FeatureFlagScreen {
    @Serializable
    data object FeatureFlagHomeScreen : FeatureFlagScreen
}

fun NavGraphBuilder.featureFlagGraph(navController: NavController) {
    navigation<FeatureFlagRoute>(
        startDestination = FeatureFlagScreen.FeatureFlagHomeScreen
    ) {
        composable<FeatureFlagScreen.FeatureFlagHomeScreen> {
            FeatureFlagScreen(
                onNavigateUp = { navController.popBackStack() },
            )
        }
    }
}