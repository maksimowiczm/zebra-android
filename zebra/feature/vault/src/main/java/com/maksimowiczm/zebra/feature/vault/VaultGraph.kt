package com.maksimowiczm.zebra.feature.vault

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.maksimowiczm.zebra.feature.vault.import_vault.ImportVaultScreen
import com.maksimowiczm.zebra.feature.vault.home.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
data object VaultRoute

internal sealed interface VaultScreen {
    @Serializable
    data object VaultHomeScreen : VaultScreen

    @Serializable
    data object ImportVaultScreen : VaultScreen
}

fun NavGraphBuilder.vaultGraph(navController: NavController) {
    navigation<VaultRoute>(
        startDestination = VaultScreen.VaultHomeScreen
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
                        route = VaultScreen.VaultHomeScreen,
                        inclusive = false,
                    )
                }
            )
        }
    }
}