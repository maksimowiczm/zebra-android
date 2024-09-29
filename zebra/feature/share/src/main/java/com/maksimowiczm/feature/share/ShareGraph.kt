package com.maksimowiczm.feature.share

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.maksimowiczm.feature.share.connection.ConnectionScreen
import com.maksimowiczm.feature.share.scanner.QrScannerScreen
import com.maksimowiczm.zebra.core.data.model.VaultEntryIdentifier
import com.maksimowiczm.zebra.core.data.model.VaultIdentifier
import kotlinx.serialization.Serializable

@Serializable
data object ShareRoute

sealed interface ShareScreen {
    @Serializable
    data object ShareUnknownEntryScreen : ShareScreen

    @Serializable
    data class ShareEntryScreen(
        val vaultIdentifier: VaultIdentifier,
        val entryIdentifier: VaultEntryIdentifier,
    ) : ShareScreen

    @Serializable
    data class ConnectionScreen(
        val vaultIdentifier: VaultIdentifier,
        val entryIdentifier: VaultEntryIdentifier,
        val session: String,
    ) : ShareScreen
}

fun NavGraphBuilder.shareGraph(navController: NavController) {
    navigation<ShareRoute>(
        startDestination = ShareScreen.ShareUnknownEntryScreen
    ) {
        composable<ShareScreen.ShareUnknownEntryScreen> {}
        composable<ShareScreen.ShareEntryScreen> {
            val route = it.toRoute<ShareScreen.ShareEntryScreen>()

            QrScannerScreen(
                onNavigateUp = { navController.popBackStack() },
                onCode = { code ->
                    navController.navigate(
                        ShareScreen.ConnectionScreen(
                            vaultIdentifier = route.vaultIdentifier,
                            entryIdentifier = route.entryIdentifier,
                            session = code,
                        )
                    )
                }
            )
        }
        composable<ShareScreen.ConnectionScreen> {
            ConnectionScreen(
                onSuccessBack = {
                    navController.popBackStack(
                        route = ShareRoute,
                        inclusive = true
                    )
                },
                onFailureBack = { navController.popBackStack() }
            )
        }
    }
}