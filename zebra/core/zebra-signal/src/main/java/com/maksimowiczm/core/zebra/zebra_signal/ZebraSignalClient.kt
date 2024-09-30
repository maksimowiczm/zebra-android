package com.maksimowiczm.core.zebra.zebra_signal

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import okhttp3.OkHttpClient


// todo error handling, currently there is literally none
class ZebraSignalClient(
    private val ioDispatcher: CoroutineDispatcher,
    private val url: String,
) {
    suspend fun ping(): Boolean {
        try {
            val request = okhttp3.Request.Builder()
                .url("$url/ping")
                .build()

            val response = withContext(ioDispatcher) {
                return@withContext OkHttpClient().newCall(request).execute()
            }

            return response.isSuccessful
        } catch (e: Exception) {
            return false
        }
    }

    fun <M> getSocket(
        serializer: KSerializer<M>,
        token: String,
    ): Socket<M> {
        val request = okhttp3.Request.Builder()
            .url("$url/ws?token=$token")
            .build()

        val socket = ZebraSocket(
            { l -> OkHttpClient().newWebSocket(request, l) },
            serializer,
        )

        return socket
    }

    suspend fun getSession(): String {
        val request = okhttp3.Request.Builder()
            .url("$url/session")
            .build()

        val response = withContext(ioDispatcher) {
            return@withContext OkHttpClient().newCall(request).execute()
        }

        val body = response.body!!
        val token = body.string()

        return token
    }
}
