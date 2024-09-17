package com.maksimowiczm.zebra.core.biometry

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Log
import com.maksimowiczm.zebra.core.data.crypto.CryptoContext
import com.maksimowiczm.zebra.core.data.crypto.CryptoResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


// TODO [#1]
// If key is invalidated on encrypt or decrypt the KeyPermanentlyInvalidatedException is thrown.
// This exception should be handled and the key should be re-initialized.
// Other services should be notified about the invalidation.
// Maybe store random number which indicates the key version and invalidate the key when the number changes.
// This would require persisting the number somewhere.

/**
 * Context for encrypting and decrypting data using biometric authentication.
 * The key is tied to the biometric authentication and is invalidated when the user re-enrolls.
 */
class BiometricCryptoContext(
    private val biometricManager: BiometricManager,
    private val defaultDispatcher: CoroutineDispatcher,
) : CryptoContext {
    private val keyStore: KeyStore
        get() = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

    private val secretKey: SecretKey
        get() {
            val key = keyStore.getKey(ALIAS, null) as? SecretKey
            if (key != null) {
                return key
            }

            initializeKey()

            return keyStore.getKey(ALIAS, null) as SecretKey
        }

    private fun initializeKey() {
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
    }

    private val cipher: Cipher
        get() = Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_GCM + "/"
                    + KeyProperties.ENCRYPTION_PADDING_NONE
        )

    override suspend fun encrypt(data: ByteArray): CryptoResult {
        return withContext(defaultDispatcher) {
            val cipher = try {
                cipher.apply {
                    init(Cipher.ENCRYPT_MODE, secretKey)
                }
            } catch (e: KeyPermanentlyInvalidatedException) {
                initializeKey()
                return@withContext CryptoResult.PermanentlyInvalidated
            }

            val authCipher = biometricManager.authenticate(cipher).firstOrNull {
                return@firstOrNull it is AuthenticationResult.Success
            }?.let {
                (it as AuthenticationResult.Success).cipher
            }

            if (authCipher == null) {
                return@withContext CryptoResult.Failed
            }

            val encrypted = authCipher.doFinal(data)

            val iv = authCipher.iv
            val combined = ByteArray(encrypted.size + iv.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)

            CryptoResult.Success(combined)
        }
    }

    override suspend fun decrypt(data: ByteArray): CryptoResult {
        return withContext(defaultDispatcher) {
            val encrypted = data.copyOfRange(12, data.size)
            val iv = data.copyOfRange(0, 12)

            val cipher = try {
                cipher.apply {
                    init(
                        Cipher.DECRYPT_MODE,
                        keyStore.getKey(ALIAS, null),
                        GCMParameterSpec(128, iv)
                    )
                }
            } catch (e: KeyPermanentlyInvalidatedException) {
                initializeKey()
                return@withContext CryptoResult.PermanentlyInvalidated
            }

            val authCipher = biometricManager.authenticate(cipher).firstOrNull {
                return@firstOrNull it is AuthenticationResult.Success
            }?.let {
                (it as AuthenticationResult.Success).cipher
            }

            if (authCipher == null) {
                return@withContext CryptoResult.Failed
            }

            val final = authCipher.doFinal(encrypted)

            CryptoResult.Success(final)
        }
    }

    companion object {
        private const val ALIAS = "biometric_key"
        private const val TAG = "BiometricCrypto"
    }
}