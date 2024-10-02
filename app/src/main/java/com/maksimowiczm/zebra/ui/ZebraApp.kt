package com.maksimowiczm.zebra.ui

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
fun ZebraApp(
    biometricManager: BiometricManager,
) {
    ZebraTheme {
        Scaffold { padding ->
            Column(
                modifier = Modifier.padding(padding)
            ) {
                if (LocalContext.current.applicationInfo.flags.and(ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(text = "DEBUGGABLE")
                    }
                }

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