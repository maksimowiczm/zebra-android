package com.maksimowiczm.zebra.core.data.fake.repository

import com.maksimowiczm.zebra.core.data.api.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeUserPreferencesRepository @Inject constructor() :
    UserPreferencesRepository {
    override fun observeBiometricIdentifier(): Flow<ByteArray> {
        return flow {
            emit(byteArrayOf())
        }
    }

    override suspend fun getBiometricIdentifier(): ByteArray {
        return byteArrayOf()
    }

    override suspend fun setBiometricIdentifier(identifier: ByteArray) {

    }
}