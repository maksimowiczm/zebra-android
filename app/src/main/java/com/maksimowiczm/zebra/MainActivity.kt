package com.maksimowiczm.zebra

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.maksimowiczm.zebra.core.biometry.BiometricManager
import com.maksimowiczm.zebra.core.data.repository.UserPreferencesRepository
import com.maksimowiczm.zebra.ui.ZebraApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val biometricManager = BiometricManager(
            fragmentActivity = this,
            mainDispatcher = Dispatchers.Main,
            defaultDispatcher = Dispatchers.Default,
            userPreferencesRepository = userPreferencesRepository,
        )

        setContent {
            ZebraApp(
                biometricManager = biometricManager
            )
        }
    }
}