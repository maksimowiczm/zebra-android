package com.maksimowiczm.zebra.core.data.fake.repository

import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.api.repository.ZebraSignalRepository
import com.maksimowiczm.zebra.core.zebra_signal.ZebraSignalClient
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FakeZebraSignalRepository @Inject constructor() : ZebraSignalRepository {
    override suspend fun pingZebraSignalClient(signalingServerUrl: String): Result<Unit, ZebraSignalClient.RequestError> {
        TODO("Not yet implemented")
    }

    override fun observeZebraSignalUrl(): Flow<String> {
        TODO("Not yet implemented")
    }

    override suspend fun updateZebraSignalUrl(signalingServerUrl: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getZebraSignalClient(): ZebraSignalClient {
        TODO("Not yet implemented")
    }
}