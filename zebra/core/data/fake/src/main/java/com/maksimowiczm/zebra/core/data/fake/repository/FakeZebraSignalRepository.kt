package com.maksimowiczm.zebra.core.data.fake.repository

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.api.repository.ZebraSignalRepository
import com.maksimowiczm.zebra.core.zebra_signal.ZebraSignalClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeZebraSignalRepository @Inject constructor() : ZebraSignalRepository {
    override suspend fun pingZebraSignalClient(signalingServerUrl: String): Result<Unit, ZebraSignalClient.RequestError> {
        return Ok(Unit)
    }

    override fun observeZebraSignalUrl(): Flow<String> {
        return flow { emit("https://github.com/maksimowiczm/zebra-signal") }
    }

    override suspend fun updateZebraSignalUrl(signalingServerUrl: String) {
    }

    override suspend fun getZebraSignalClient(): ZebraSignalClient {
        TODO()
    }
}