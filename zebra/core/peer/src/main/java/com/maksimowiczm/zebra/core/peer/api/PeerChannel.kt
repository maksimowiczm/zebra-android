package com.maksimowiczm.zebra.core.peer.api


interface PeerChannel {
    enum class Status {
        UNKNOWN,
        CONNECTING,
        CONNECTED,
        CLOSED,
        FAILED
    }

    fun send(message: String): Boolean
    fun send(message: ByteArray): Boolean

    fun close()

    interface Listener {
        /**
         * Called when a message has been received.
         */
        fun onMessage(channel: PeerChannel, message: ByteArray)

        /**
         * Called when the channel has been opened and is ready to send and receive messages.
         */
        fun onOpen(channel: PeerChannel)

        /**
         * Called when the channel has been closed.
         * The channel is not usable after this call.
         */
        fun onClosed(channel: PeerChannel)

        /**
         * Called when the channel has failed to connect or has been disconnected unexpectedly.
         * Channel doesn't necessarily have to exist yet when this is called.
         * The channel is not usable after this call.
         */
        fun onFailure(channel: PeerChannel?, cause: Throwable)
    }
}