package com.maksimowiczm.zebra.core.data.repository

import com.maksimowiczm.zebra.core.datastore.UserPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserPreferencesRepository @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
) {
    fun observeBiometricIdentifier(): Flow<ByteArray> {
        return userPreferencesDataSource.observeBiometricIdentifier()
    }

    suspend fun getBiometricIdentifier(): ByteArray {
        return userPreferencesDataSource.getBiometricIdentifier()
    }

    suspend fun setBiometricIdentifier(identifier: ByteArray) {
        return userPreferencesDataSource.updateBiometricIdentifier(identifier)
    }
}