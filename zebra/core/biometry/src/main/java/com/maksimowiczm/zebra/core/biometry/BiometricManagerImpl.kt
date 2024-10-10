package com.maksimowiczm.zebra.core.biometry

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import javax.crypto.Cipher
import androidx.biometric.BiometricManager as AndroidBiometricManager
import com.maksimowiczm.zebra.core.biometry.BiometricManager as BiometricManagerAPI

class BiometricManagerImpl(
    private val fragmentActivity: FragmentActivity,
    private val mainDispatcher: CoroutineDispatcher, // UI thread
    private val defaultDispatcher: CoroutineDispatcher, // CPU bound work
    private val userPreferencesRepository: com.maksimowiczm.zebra.core.data.api.repository.UserPreferencesRepository,
) : BiometricManagerAPI {
    private val biometricManager = AndroidBiometricManager.from(fragmentActivity)

    override fun hasBiometric(): BiometryStatus {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometryStatus.Ok
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometryStatus.NotEnrolled
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometryStatus.NotAvailable
            else -> BiometryStatus.NotSupported
        }
    }

    private fun getPromptInfo(context: Context): PromptInfo {
        return PromptInfo.Builder().apply {
            setTitle(context.getString(R.string.biometric_prompt_title))
            setNegativeButtonText(context.getString(R.string.cancel))
            setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        }.build()
    }

    override fun authenticate(cipher: Cipher?): Flow<AuthenticationResult> = channelFlow {
        if (hasBiometric() != BiometryStatus.Ok) {
            send(AuthenticationResult.Error)
            close()
            return@channelFlow
        }

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Authentication succeeded")
                trySend(AuthenticationResult.Success(result.cryptoObject?.cipher))
                close()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                trySend(AuthenticationResult.Failed)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(TAG, "Authentication error: $errorCode $errString")
                trySend(AuthenticationResult.Error)
                close()
            }
        }

        val prompt = BiometricPrompt(
            fragmentActivity,
            callback
        )

        val promptInfo = getPromptInfo(fragmentActivity)
        val cryptoObject = cipher?.let { CryptoObject(it) }

        if (cryptoObject != null) {
            Log.d(TAG, "Authenticating with crypto object")

            withContext(mainDispatcher) {
                prompt.authenticate(promptInfo, cryptoObject)
            }
        } else {
            Log.d(TAG, "Authenticating without crypto object")

            withContext(mainDispatcher) {
                prompt.authenticate(promptInfo)
            }
        }

        awaitClose {}
    }

    /**
     * Provides access to the cryptographic operations with the biometric prompt.
     */
    override val cryptoContext
        get() = BiometricCryptoContext(
            biometricManager = this,
            defaultDispatcher = defaultDispatcher,
            userPreferencesRepository = userPreferencesRepository,
        )

    companion object {
        private const val TAG = "BiometricManager"
    }
}