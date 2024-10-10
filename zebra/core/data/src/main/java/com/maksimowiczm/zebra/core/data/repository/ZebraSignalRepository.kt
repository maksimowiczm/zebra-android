package com.maksimowiczm.zebra.core.data.repository

import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.api.repository.ZebraSignalRepository
import com.maksimowiczm.zebra.core.zebra_signal.ZebraSignalClient
import com.maksimowiczm.zebra.core.zebra_signal.ZebraSignalClientFactory
import com.maksimowiczm.zebra.core.datastore.UserPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ZebraSignalRepositoryImpl @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val clientFactory: ZebraSignalClientFactory,
) : ZebraSignalRepository {
    override suspend fun pingZebraSignalClient(signalingServerUrl: String): Result<Unit, ZebraSignalClient.RequestError> {
        return clientFactory.create(signalingServerUrl).ping()
    }

    override fun observeZebraSignalUrl(): Flow<String> {
        return userPreferencesDataSource.observeSignalingServerUrl()
    }

    override suspend fun updateZebraSignalUrl(signalingServerUrl: String) {
        userPreferencesDataSource.updateSignalingServerUrl(signalingServerUrl)
    }

    override suspend fun getZebraSignalClient(): ZebraSignalClient {
        val signalingServerUrl = userPreferencesDataSource.observeSignalingServerUrl().first()
        return clientFactory.create(signalingServerUrl)
    }
}