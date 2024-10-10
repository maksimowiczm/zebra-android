package com.maksimowiczm.zebra.core.data.api.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun observeBiometricIdentifier(): Flow<ByteArray>
    suspend fun getBiometricIdentifier(): ByteArray
    suspend fun setBiometricIdentifier(identifier: ByteArray)
}