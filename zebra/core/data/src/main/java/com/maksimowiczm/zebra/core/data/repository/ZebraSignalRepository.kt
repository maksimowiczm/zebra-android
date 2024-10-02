package com.maksimowiczm.zebra.core.data.repository

import com.maksimowiczm.zebra.core.zebra_signal.ZebraSignalClient
import com.maksimowiczm.zebra.core.zebra_signal.ZebraSignalClientFactory
import com.maksimowiczm.zebra.core.datastore.UserPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ZebraSignalRepository @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val clientFactory: ZebraSignalClientFactory
) {
    fun observeZebraSignalUrl(): Flow<String> {
        return userPreferencesDataSource.observeSignalingServerUrl()
    }

    suspend fun updateZebraSignalUrl(signalingServerUrl: String) {
        userPreferencesDataSource.updateSignalingServerUrl(signalingServerUrl)
    }

    suspend fun getZebraSignalClient(): ZebraSignalClient {
        val signalingServerUrl = userPreferencesDataSource.observeSignalingServerUrl().first()
        return clientFactory.create(signalingServerUrl)
    }
}