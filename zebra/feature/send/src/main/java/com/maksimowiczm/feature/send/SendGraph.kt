package com.maksimowiczm.feature.send

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.maksimowiczm.feature.send.scanner.QrScannerScreen
import com.maksimowiczm.zebra.core.data.model.VaultEntryIdentifier
import com.maksimowiczm.zebra.core.data.model.VaultIdentifier
import kotlinx.serialization.Serializable

@Serializable
data object SendRoute

sealed interface SendScreen {
    @Serializable
    data object SendUnknownEntryScreen : SendScreen

    @Serializable
    data class SendEntryScreen(
        val vaultIdentifier: VaultIdentifier,
        val entryIdentifier: VaultEntryIdentifier,
    ) : SendScreen

    @Serializable
    data class ConnectionScreen(
        val vaultIdentifier: VaultIdentifier,
        val entryIdentifier: VaultEntryIdentifier,
    ) : SendScreen
}

fun NavGraphBuilder.sendGraph(navController: NavController) {
    navigation<SendRoute>(
        startDestination = SendScreen.SendUnknownEntryScreen
    ) {
        composable<SendScreen.SendUnknownEntryScreen> {}
        composable<SendScreen.SendEntryScreen> {
            val route = it.toRoute<SendScreen.SendEntryScreen>()

            QrScannerScreen(
                onNavigateUp = { navController.popBackStack() },
                onCode = {
                    navController.navigate(
                        SendScreen.ConnectionScreen(
                            vaultIdentifier = route.vaultIdentifier,
                            entryIdentifier = route.entryIdentifier
                        )
                    )
                }
            )
        }
        composable<SendScreen.ConnectionScreen> {}
    }
}