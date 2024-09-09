package com.maksimowiczm.zebra.feature_vault

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.maksimowiczm.zebra.common_ui.ComposableScreen
import com.maksimowiczm.zebra.feature_vault.home.HomeScreen
import kotlinx.serialization.Serializable

internal sealed interface VaultScreen : ComposableScreen {
    @Serializable
    data object VaultHomeScreen : VaultScreen
}

const val VAULT_ROUTE = "VAULT"

fun NavGraphBuilder.vaultGraph(navController: NavController) {
    navigation(
        route = VAULT_ROUTE,
        startDestination = VaultScreen.VaultHomeScreen.toDestination()
    ) {
        composable<VaultScreen.VaultHomeScreen> {
            HomeScreen(
                viewModel = viewModel(),
                onAdd = {}
            )
        }
    }
}