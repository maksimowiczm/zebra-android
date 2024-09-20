package com.maksimowiczm.zebra.core.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import com.google.protobuf.ByteString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.SecureRandom

class UserPreferencesDataSource(
    private val userPreferencesStore: DataStore<UserPreferences>,
) {
    fun observeBiometricIdentifier(): Flow<ByteArray> {
        return userPreferencesStore.data.map { it.biometricIdentifier.toByteArray() }
    }

    suspend fun getBiometricIdentifier(): ByteArray {
        return userPreferencesStore
            .data
            .catch {
                Log.e(TAG, "Error reading biometric identifier.", it)
                throw it
            }.first()
            .biometricIdentifier
            .toByteArray()
    }

    suspend fun updateBiometricIdentifier(identifier: ByteArray) {
        runCatching {
            userPreferencesStore.updateData { currentPreferences ->
                currentPreferences
                    .toBuilder()
                    .setBiometricIdentifier(ByteString.copyFrom(identifier))
                    .build()
            }
        }.getOrElse {
            Log.e(TAG, "Error updating biometric identifier.", it)
            throw it
        }
    }

    suspend fun getPersistentIdentifier(): ByteArray {
        return try {
            userPreferencesStore
                .data
                .first()
                .persistentIdentifier
                .toByteArray()
        } catch (e: IOException) {
            Log.w(TAG, "Error reading persistent identifier.", e)

            // Generate a new persistent identifier if one doesn't exist.
            val random = SecureRandom.getInstanceStrong()
            val newIdentifier = ByteArray(32).apply { random.nextBytes(this) }

            userPreferencesStore.updateData { currentPreferences ->
                currentPreferences
                    .toBuilder()
                    .setPersistentIdentifier(ByteString.copyFrom(newIdentifier))
                    .build()
            }

            newIdentifier
        }
    }


    companion object {
        private const val TAG = "UserPreferencesDataSource"
    }
}