package com.maksimowiczm.zebra.core.zebra_signal

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Factory for creating [ZebraSignalClient] instances.
 * Useful for dependency injection.
 * @param ioDispatcher The [CoroutineDispatcher] to use for IO operations.
 * @see ZebraSignalClient
 */
class ZebraSignalClientFactory(private val ioDispatcher: CoroutineDispatcher) {
    fun create(url: String) = ZebraSignalClient(
        url = url,
        ioDispatcher = ioDispatcher
    )
}