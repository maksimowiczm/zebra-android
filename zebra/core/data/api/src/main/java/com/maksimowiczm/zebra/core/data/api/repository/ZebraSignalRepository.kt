package com.maksimowiczm.zebra.core.data.api.repository

import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.zebra_signal.ZebraSignalClient
import kotlinx.coroutines.flow.Flow

interface ZebraSignalRepository {
    suspend fun pingZebraSignalClient(signalingServerUrl: String): Result<Unit, ZebraSignalClient.RequestError>
    fun observeZebraSignalUrl(): Flow<String>
    suspend fun updateZebraSignalUrl(signalingServerUrl: String)
    suspend fun getZebraSignalClient(): ZebraSignalClient
}