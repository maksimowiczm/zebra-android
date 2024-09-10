package com.maksimowiczm.zebra.feature.vault

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.maksimowiczm.zebra.core.common_ui.ComposableScreen
import com.maksimowiczm.zebra.feature.vault.import_vault.ImportVaultScreen
import com.maksimowiczm.zebra.feature.vault.home.HomeScreen
import kotlinx.serialization.Serializable

internal sealed interface VaultScreen : ComposableScreen {
    @Serializable
    data object VaultHomeScreen : VaultScreen

    @Serializable
    data object ImportVaultScreen : VaultScreen
}

const val VAULT_ROUTE = "VAULT"

fun NavGraphBuilder.vaultGraph(navController: NavController) {
    navigation(
        route = VAULT_ROUTE,
        startDestination = VaultScreen.VaultHomeScreen.toDestination()
    ) {
        composable<VaultScreen.VaultHomeScreen> {
            HomeScreen(
                onImport = {
                    navController.navigate(VaultScreen.ImportVaultScreen)
                }
            )
        }
        composable<VaultScreen.ImportVaultScreen> {
            ImportVaultScreen(
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