package com.maksimowiczm.zebra.core.zebra_signal

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

internal class ZebraSocket<M>(
    webSocketFactory: (listener: WebSocketListener) -> WebSocket,
    private val serializer: KSerializer<M>,
) : Socket<M> {
    private val listeners = mutableSetOf<Socket.Listener<M>>()
    private val socket: WebSocket = webSocketFactory(MainListener())

    override fun addListener(listener: Socket.Listener<M>) {
        listeners.add(listener)
    }

    override fun removeListener(listener: Socket.Listener<M>) {
        listeners.remove(listener)
    }

    override fun send(message: M): Boolean {
        val json = Json.encodeToString(serializer, message)

        return socket.send(json)
    }

    override fun close(code: Int, reason: String?): Boolean {
        return socket.close(code, reason)
    }

    private inner class MainListener : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            val message = Json.decodeFromString(serializer, text)
            listeners.onEach { it.onMessage(this@ZebraSocket, message) }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            val asString = bytes.string(Charsets.UTF_8)
            onMessage(webSocket, asString)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            listeners.onEach { it.onClosed(this@ZebraSocket, code, reason) }
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            listeners.onEach { it.onOpen(this@ZebraSocket) }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            listeners.onEach { it.onFailure(this@ZebraSocket, t) }
        }
    }

    companion object {
        private val Json = Json { ignoreUnknownKeys = true }
    }
}