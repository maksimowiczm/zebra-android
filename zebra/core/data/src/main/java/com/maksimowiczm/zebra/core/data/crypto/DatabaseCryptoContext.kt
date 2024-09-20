package com.maksimowiczm.zebra.core.data.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.repository.UserPreferencesRepository
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

/**
 * Context which should be used for encrypting and decrypting database entities.
 */
class DatabaseCryptoContext @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesRepository,
) : CryptoContext {
    override suspend fun getIdentifier() = userPreferencesDataSource.getPersistentIdentifier()

    override suspend fun encrypt(data: ByteArray): Result<ByteArray, EncryptError> {
        return try {
            cipher.apply {
                init(Cipher.ENCRYPT_MODE, secretKey)
            }

            val iv = cipher.iv
            val encryptedData = cipher.doFinal(data)

            Ok(iv + encryptedData)
        } catch (e: Exception) {
            Err(EncryptError.Unknown)
        }
    }

    override suspend fun decrypt(data: ByteArray): Result<ByteArray, DecryptError> {
        val iv = data.sliceArray(0 until 12)
        val encryptedData = data.sliceArray(12 until data.size)

        return try {
            cipher.apply {
                init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            }

            Ok(cipher.doFinal(encryptedData))
        } catch (e: Exception) {
            Err(DecryptError.Unknown)
        }
    }

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

    companion object {
        private const val TAG = "DatabaseCryptoContext"
        private const val ALIAS = "db_key"
    }
}