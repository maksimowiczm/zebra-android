package com.maksimowiczm.zebra.core.data.repository

import com.maksimowiczm.zebra.core.data.api.repository.UserPreferencesRepository
import com.maksimowiczm.zebra.core.datastore.UserPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class UserPreferencesRepositoryImpl @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
) : UserPreferencesRepository {
    override fun observeBiometricIdentifier(): Flow<ByteArray> {
        return userPreferencesDataSource.observeBiometricIdentifier()
    }

    override suspend fun getBiometricIdentifier(): ByteArray {
        return userPreferencesDataSource.getBiometricIdentifier()
    }

    override suspend fun setBiometricIdentifier(identifier: ByteArray) {
        return userPreferencesDataSource.updateBiometricIdentifier(identifier)
    }
}