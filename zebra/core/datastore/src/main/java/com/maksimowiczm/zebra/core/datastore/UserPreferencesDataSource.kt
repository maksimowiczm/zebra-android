package com.maksimowiczm.zebra.core.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import com.google.protobuf.ByteString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

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
                currentPreferences.toBuilder()
                    .setBiometricIdentifier(ByteString.copyFrom(identifier)).build()
            }
        }.getOrElse {
            Log.e(TAG, "Error updating biometric identifier.", it)
            throw it
        }
    }

    fun observeFeatureFlag(name: String, default: Boolean): Flow<Boolean> {
        return userPreferencesStore.data.map { preferences ->
            preferences.featureFlagsMap[name] ?: default
        }
    }

    suspend fun updateFeatureFlag(name: String, isEnabled: Boolean) {
        runCatching {
            userPreferencesStore.updateData { currentPreferences ->
                currentPreferences
                    .toBuilder()
                    .putFeatureFlags(name, isEnabled)
                    .build()
            }
        }.getOrElse {
            Log.e(TAG, "Error updating feature flag.", it)
            throw it
        }
    }

    fun observeSignalingServerUrl(): Flow<String> {
        return userPreferencesStore.data.map { it.signalingServerUrl }
    }

    suspend fun updateSignalingServerUrl(url: String?) {
        runCatching {
            userPreferencesStore.updateData { currentPreferences ->
                currentPreferences.toBuilder()
                    .setSignalingServerUrl(url)
                    .build()
            }
        }.getOrElse {
            Log.e(TAG, "Error updating signaling server URL.", it)
            throw it
        }
    }

    companion object {
        private const val TAG = "UserPreferencesDataSource"
    }
}