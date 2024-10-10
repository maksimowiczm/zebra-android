package com.maksimowiczm.zebra.demo_app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.maksimowiczm.zebra.feature.share.ShareScreen
import com.maksimowiczm.zebra.feature.share.shareGraph
import com.maksimowiczm.zebra.core.biometry.BiometricManager
import com.maksimowiczm.zebra.core.common_ui.theme.ZebraTheme
import com.maksimowiczm.zebra.feature.feature_flag.FeatureFlagRoute
import com.maksimowiczm.zebra.feature.feature_flag.featureFlagGraph
import com.maksimowiczm.zebra.feature.vault.VaultRoute
import com.maksimowiczm.zebra.feature.vault.vaultGraph

@Composable
fun ZebraDemoApp(
    biometricManager: BiometricManager,
) {
    ZebraTheme {
        Scaffold { padding ->
            Column(modifier = Modifier.padding(padding)) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = VaultRoute,
                ) {
                    vaultGraph(
                        navController = navController,
                        biometricManager = biometricManager,
                        onNavigateSend = { vaultIdentifier, entryIdentifier ->
                            navController.navigate(
                                ShareScreen.ShareEntryScreen(
                                    vaultIdentifier,
                                    entryIdentifier
                                )
                            )
                        },
                        onNavigateFeatureFlag = { navController.navigate(FeatureFlagRoute) }
                    )
                    shareGraph(navController = navController)
                    featureFlagGraph(
                        navController = navController,
                        onShareToggle = { value, fallback ->
                            if (value) {
                                navController.navigate(ShareScreen.ShareSetupScreen)
                            } else {
                                fallback(false)
                            }
                        }
                    )
                }
            }
        }
    }
}