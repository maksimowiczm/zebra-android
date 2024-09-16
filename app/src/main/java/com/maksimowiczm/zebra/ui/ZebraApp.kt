package com.maksimowiczm.zebra.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.maksimowiczm.zebra.core.biometry.BiometricManager
import com.maksimowiczm.zebra.core.common_ui.theme.ZebraTheme
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
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = VaultRoute,
                ) {
                    vaultGraph(navController)
                }
            }
        }
    }
}