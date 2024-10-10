package com.maksimowiczm.zebra.core.biometry

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Log
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.api.repository.UserPreferencesRepository
import com.maksimowiczm.zebra.core.data.api.crypto.CryptoContext
import com.maksimowiczm.zebra.core.data.api.crypto.DecryptError
import com.maksimowiczm.zebra.core.data.api.crypto.EncryptError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Context for encrypting and decrypting data using biometric authentication.
 * The key is tied to the biometric authentication and is invalidated when the user re-enrolls.
 */
class BiometricCryptoContext(
    private val biometricManager: BiometricManager,
    private val defaultDispatcher: CoroutineDispatcher,
    private val userPreferencesRepository: UserPreferencesRepository,
) : CryptoContext {
    private val keyStore: KeyStore
        get() = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

    private suspend fun getSecretKey(): SecretKey {
        val key = keyStore.getKey(ALIAS, null) as? SecretKey
        if (key != null) {
            return key
        }

        initializeKey()

        return keyStore.getKey(ALIAS, null) as SecretKey
    }

    private suspend fun initializeKey() {
        Log.d(TAG, "Initializing key")

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        val builder = KeyGenParameterSpec.Builder(
            ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )

        builder
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)

        Log.d(TAG, "Generating new key")
        keyGenerator.init(builder.build())
        keyGenerator.generateKey()

        Log.d(TAG, "Generating new biometric identifier")
        // Make sure that new identifier is different from the current one.
        val currentIdentifier = getIdentifier()
        val identifier = ByteArray(8)
        do {
            SecureRandom.getInstanceStrong().nextBytes(identifier)
        } while (identifier.contentEquals(currentIdentifier))
        userPreferencesRepository.setBiometricIdentifier(identifier)
    }

    private val cipher: Cipher
        get() = Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_GCM + "/"
                    + KeyProperties.ENCRYPTION_PADDING_NONE
        )

    override suspend fun getIdentifier(): ByteArray {
        return userPreferencesRepository.getBiometricIdentifier()
    }

    override suspend fun encrypt(data: ByteArray): Result<ByteArray, EncryptError> {
        return withContext(defaultDispatcher) {
            val cipher = try {
                cipher.apply {
                    init(Cipher.ENCRYPT_MODE, getSecretKey())
                }
            } catch (e: KeyPermanentlyInvalidatedException) {
                initializeKey()
                cipher.apply {
                    init(Cipher.ENCRYPT_MODE, getSecretKey())
                }
            }

            val authCipher = biometricManager.authenticate(cipher).firstOrNull {
                return@firstOrNull it is AuthenticationResult.Success
            }?.let {
                (it as AuthenticationResult.Success).cipher
            }

            if (authCipher == null) {
                return@withContext Err(EncryptError.Canceled)
            }

            val encrypted = authCipher.doFinal(data)

            val iv = authCipher.iv
            val combined = ByteArray(encrypted.size + iv.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)

            Ok(combined)
        }
    }

    override suspend fun decrypt(data: ByteArray): Result<ByteArray, DecryptError> {
        return withContext(defaultDispatcher) {
            val iv = data.copyOfRange(0, 12)
            val encrypted = data.copyOfRange(12, data.size)

            val cipher = try {
                cipher.apply {
                    init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(128, iv))
                }
            } catch (e: KeyPermanentlyInvalidatedException) {
                initializeKey()
                return@withContext Err(DecryptError.PermanentlyInvalidated)
            }

            val authCipher = biometricManager.authenticate(cipher).firstOrNull {
                return@firstOrNull it is AuthenticationResult.Success
            }?.let {
                (it as AuthenticationResult.Success).cipher
            }

            if (authCipher == null) {
                return@withContext Err(DecryptError.Canceled)
            }

            val final = authCipher.doFinal(encrypted)

            Ok(final)
        }
    }

    companion object {
        private const val ALIAS = "biometric_key"
        private const val TAG = "BiometricCrypto"
    }
}