package com.maksimowiczm.core.zebra.zebra_signal

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import okhttp3.OkHttpClient
import java.io.IOException
import java.net.UnknownServiceException


// todo error handling, currently there is literally none
class ZebraSignalClient(
    private val ioDispatcher: CoroutineDispatcher,
    private val url: String,
) {
    private val client = OkHttpClient()

    sealed interface RequestError {
        data object NetworkError : RequestError
        data object NotValidURL : RequestError
        data object SerializationError : RequestError
        data class HTTPError(val code: Int) : RequestError
        data object UnsecureConnection : RequestError
        data object Unknown : RequestError
    }

    sealed interface SocketError {
        data object NotValidURL : SocketError
        data class SocketFactoryError(val t: Throwable) : SocketError
    }

    suspend fun ping(): Result<Unit, RequestError> {
        return try {
            val request = okhttp3.Request.Builder()
                .url("$url/ping")
                .build()

            val response = withContext(ioDispatcher) {
                return@withContext client.newCall(request).execute()
            }

            if (!response.isSuccessful) {
                return Err(RequestError.HTTPError(response.code))
            }

            Ok(Unit)
        } catch (_: IllegalArgumentException) {
            Err(RequestError.NotValidURL)
        } catch (_: UnknownServiceException) {
            Err(RequestError.UnsecureConnection)
        } catch (e: IOException) {
            Err(RequestError.NetworkError)
        }
    }

    fun <M> getSocket(
        serializer: KSerializer<M>,
        token: String,
    ): Result<Socket<M>, SocketError> {
        val request = try {
            okhttp3.Request.Builder()
                .url("$url/ws?token=$token")
                .build()
        } catch (_: IllegalArgumentException) {
            return Err(SocketError.NotValidURL)
        }

        val socket = try {
            ZebraSocket(
                { l -> client.newWebSocket(request, l) },
                serializer,
            )
        } catch (t: Throwable) {
            return Err(SocketError.SocketFactoryError(t))
        }

        return Ok(socket)
    }

    suspend fun getSession(): Result<String, RequestError> {
        return try {
            val request = okhttp3.Request.Builder()
                .url("$url/session")
                .build()

            val response = withContext(ioDispatcher) {
                return@withContext client.newCall(request).execute()
            }

            if (!response.isSuccessful) {
                return Err(RequestError.HTTPError(response.code))
            }

            val body = response.body ?: return Err(RequestError.Unknown)

            val token = try {
                body.string()
            } catch (_: IOException) {
                return Err(RequestError.SerializationError)
            }

            Ok(token)
        } catch (_: IllegalArgumentException) {
            Err(RequestError.NotValidURL)
        } catch (_: IOException) {
            Err(RequestError.NetworkError)
        }
    }
}
