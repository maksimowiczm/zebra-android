package com.maksimowiczm.zebra

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.maksimowiczm.zebra.core.biometry.BiometricManager
import com.maksimowiczm.zebra.ui.ZebraApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val biometricManager = BiometricManager(
            fragmentActivity = this,
            mainDispatcher = Dispatchers.Main,
            defaultDispatcher = Dispatchers.Default,
        )

        setContent {
            ZebraApp(
                biometricManager = biometricManager
            )
        }
    }
}