package com.maksimowiczm.zebra

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.maksimowiczm.zebra.core.biometry.BiometricManagerImpl
import com.maksimowiczm.zebra.core.data.api.repository.UserPreferencesRepository
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

        val biometricManager = BiometricManagerImpl(
            fragmentActivity = this,
            mainDispatcher = Dispatchers.Main,
            defaultDispatcher = Dispatchers.Default,
            userPreferencesRepository = userPreferencesRepository,
        )

        enableEdgeToEdge()
        setContent {
            ZebraApp(
                biometricManager = biometricManager
            )
        }
    }
}