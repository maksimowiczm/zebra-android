package com.maksimowiczm.zebra.feature.vault

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.maksimowiczm.zebra.core.biometry.BiometricManager
import com.maksimowiczm.zebra.core.data.model.VaultIdentifier
import com.maksimowiczm.zebra.feature.vault.biometrics.SetupScreen
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

    @Serializable
    data class BiometricsScreen(val identifier: VaultIdentifier) : VaultScreen
}

fun NavGraphBuilder.vaultGraph(
    navController: NavController,
    biometricManager: BiometricManager,
) {
    navigation<VaultRoute>(
        startDestination = VaultScreen.VaultHomeScreen
    ) {
        val navigateToHome: () -> Unit = {
            navController.popBackStack(
                route = VaultScreen.VaultHomeScreen,
                inclusive = false,
            )
        }
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
                onNavigateUp = navigateToHome
            )
        }
        composable<VaultScreen.UnlockVaultScreen> {
            val route = it.toRoute<VaultScreen.UnlockVaultScreen>()

            UnlockVaultScreen(
                onNavigateUp = navigateToHome,
                onOpen = {
                    navController.navigate(VaultScreen.OpenedVaultScreen(route.identifier))
                },
                biometricManager = biometricManager,
                onBiometrics = {
                    navController.navigate(VaultScreen.BiometricsScreen(route.identifier))
                }
            )
        }
        composable<VaultScreen.OpenedVaultScreen> {
            OpenedVaultScreen(
                onNavigateUp = navigateToHome,
                onClose = navigateToHome,
            )
        }
        composable<VaultScreen.BiometricsScreen> {
            SetupScreen(
                onNavigateUp = { navController.popBackStack() },
                biometricManager = biometricManager,
            )
        }
    }
}