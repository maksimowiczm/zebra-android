package com.maksimowiczm.zebra.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

enum class NetworkStatus {
    Online,
    Offline,
}

class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun observeNetworkStatus() = callbackFlow {
        trySend(NetworkStatus.Offline)

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySend(NetworkStatus.Online)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(NetworkStatus.Offline)
            }
        }

        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
}