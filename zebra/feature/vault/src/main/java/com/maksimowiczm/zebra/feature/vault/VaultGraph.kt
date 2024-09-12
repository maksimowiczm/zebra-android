package com.maksimowiczm.zebra.feature.vault

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.maksimowiczm.zebra.core.data.model.VaultIdentifier
import com.maksimowiczm.zebra.feature.vault.import_vault.ImportVaultScreen
import com.maksimowiczm.zebra.feature.vault.home.HomeScreen
import com.maksimowiczm.zebra.feature.vault.opened.OpenedVaultScreen
import com.maksimowiczm.zebra.feature.vault.unlock.UnlockVaultScreen
import kotlinx.serialization.Serializable

@Serializable
data object VaultRoute

internal sealed interface VaultScreen {
    @Serializable
    data object VaultHomeScreen : VaultScreen

    @Serializable
    data object ImportVaultScreen : VaultScreen

    @Serializable
    data class UnlockVaultScreen(val identifier: VaultIdentifier) : VaultScreen

    @Serializable
    data class OpenedVaultScreen(val identifier: VaultIdentifier) : VaultScreen
}

fun NavGraphBuilder.vaultGraph(navController: NavController) {
    navigation<VaultRoute>(
        startDestination = VaultScreen.VaultHomeScreen
    ) {
        composable<VaultScreen.VaultHomeScreen> {
            HomeScreen(
                onImport = {
                    navController.navigate(VaultScreen.ImportVaultScreen)
                },
                onVaultClick = {
                    navController.navigate(VaultScreen.UnlockVaultScreen(it.identifier))
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
        composable<VaultScreen.UnlockVaultScreen> {
            val route = it.toRoute<VaultScreen.UnlockVaultScreen>()

            UnlockVaultScreen(
                onNavigateUp = {
                    navController.popBackStack(
                        route = VaultScreen.VaultHomeScreen,
                        inclusive = false,
                    )
                },
                onOpen = {
                    navController.navigate(VaultScreen.OpenedVaultScreen(route.identifier))
                }
            )
        }
        composable<VaultScreen.OpenedVaultScreen> {
            val route = it.toRoute<VaultScreen.OpenedVaultScreen>()

            OpenedVaultScreen(
                onNavigateUp = {
                    navController.popBackStack(
                        route = VaultScreen.VaultHomeScreen,
                        inclusive = false,
                    )
                },
                onLost = {
                    navController.navigate(VaultScreen.UnlockVaultScreen(route.identifier))
                }
            )
        }
    }
}