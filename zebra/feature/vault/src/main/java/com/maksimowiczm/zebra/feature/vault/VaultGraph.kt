package com.maksimowiczm.zebra.feature.vault

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.maksimowiczm.zebra.core.common_ui.ComposableScreen
import com.maksimowiczm.zebra.feature.vault.add_vault.AddVaultScreen
import com.maksimowiczm.zebra.feature.vault.home.HomeScreen
import kotlinx.serialization.Serializable

internal sealed interface VaultScreen : ComposableScreen {
    @Serializable
    data object VaultHomeScreen : VaultScreen

    @Serializable
    data object AddVaultScreen : VaultScreen
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
                onAdd = {
                    navController.navigate(VaultScreen.AddVaultScreen)
                }
            )
        }
        composable<VaultScreen.AddVaultScreen> {
            AddVaultScreen(
                viewModel = viewModel(),
                onNavigateUp = {
                    navController.popBackStack(
                        route = VaultScreen.VaultHomeScreen.toDestination(),
                        inclusive = false,
                    )
                }
            )
        }
    }
}