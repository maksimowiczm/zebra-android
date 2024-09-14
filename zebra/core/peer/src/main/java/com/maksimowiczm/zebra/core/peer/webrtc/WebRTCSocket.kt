package com.maksimowiczm.zebra.core.peer.webrtc

import android.util.Log
import com.maksimowiczm.zebra.core.peer.socket.Socket
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import okhttp3.Response
import okhttp3.WebSocket
import okio.ByteString

internal class WebRTCSocket(
    webSocketFactory: (listener: okhttp3.WebSocketListener) -> WebSocket,
    private val serializer: KSerializer<WebRTCMessage>,
) : Socket<WebRTCMessage> {
    companion object {
        private const val TAG = "WebRTCSocket"
        private val Json = Json { ignoreUnknownKeys = true }
    }

    private val listeners = mutableListOf<Socket.Listener<WebRTCMessage>>()
    private val socket: WebSocket = webSocketFactory(MainListener())

    override fun addListener(listener: Socket.Listener<WebRTCMessage>) {
        listeners.add(listener)
    }

    override fun removeListener(listener: Socket.Listener<WebRTCMessage>) {
        listeners.remove(listener)
    }

    override fun send(message: WebRTCMessage): Boolean {
        Log.d(TAG, "Sending message")
        val json = Json.encodeToString(serializer, message)

        if (socket.send(json)) {
            Log.d(TAG, "Message sent")
            return true
        }

        Log.w(TAG, "Failed to send message")
        return false
    }

    override fun close(code: Int, reason: String?): Boolean {
        if (socket.close(code, reason)) {
            Log.i(TAG, "Closing socket: $code, $reason")
            return true
        }

        Log.d(TAG, "Socket already closed")
        return false
    }

    private inner class MainListener : okhttp3.WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            runCatching {
                val message = Json.decodeFromString(serializer, text)
                Log.d(TAG, "Received message")
                listeners.onEach { it.onMessage(this@WebRTCSocket, message) }
            }.getOrElse {
                Log.e(TAG, "Failed to parse message: $text", it)
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d(TAG, "Received bytes with length: ${bytes.size}")
            Log.d(TAG, "Trying to parse bytes as WebRTCMessage")
            runCatching {
                val asString = bytes.string(Charsets.UTF_8)
                onMessage(webSocket, asString)
            }.getOrElse {
                Log.e(TAG, "Failed to parse bytes as WebRTCMessage", it)
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.i(TAG, "WebSocket closed: $code, $reason")
            listeners.onEach { it.onClosed(this@WebRTCSocket, code, reason) }
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.i(TAG, "WebSocket opened")
            listeners.onEach { it.onOpen(this@WebRTCSocket) }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket failure: $t")
            listeners.onEach { it.onFailure(this@WebRTCSocket, t) }
        }
    }
}
