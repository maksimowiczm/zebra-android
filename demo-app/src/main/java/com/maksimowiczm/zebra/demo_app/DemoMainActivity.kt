package com.maksimowiczm.zebra.demo_app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.maksimowiczm.zebra.core.biometry.FakeBiometricManager
import com.maksimowiczm.zebra.core.data.api.repository.UserPreferencesRepository
import com.maksimowiczm.zebra.demo_app.ui.ZebraDemoApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@AndroidEntryPoint
class DemoMainActivity : FragmentActivity() {
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val biometricManager = FakeBiometricManager(
            fragmentActivity = this,
            mainDispatcher = Dispatchers.Main,
            defaultDispatcher = Dispatchers.Default,
            userPreferencesRepository = userPreferencesRepository,
        )

        enableEdgeToEdge()
        setContent {
            ZebraDemoApp(
                biometricManager = biometricManager
            )
        }
    }
}