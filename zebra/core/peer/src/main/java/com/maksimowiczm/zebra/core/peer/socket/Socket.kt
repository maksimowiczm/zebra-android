package com.maksimowiczm.zebra.core.peer.socket

/**
 * A socket that can send and receive messages of type [M].
 */
interface Socket<M> {
    /**
     * Sends a message over the socket.
     * @return true if the message was sent successfully, false otherwise
     */
    fun send(message: M): Boolean

    /**
     * Closes the socket with the given code and reason.
     * @return true if the socket closure was initiated successfully, false otherwise
     */
    fun close(code: Int, reason: String?): Boolean

    fun addListener(listener: Listener<M>)
    fun removeListener(listener: Listener<M>)

    interface Listener<M> {
        fun onMessage(socket: Socket<M>, message: M)
        fun onFailure(socket: Socket<M>, t: Throwable)
        fun onClosed(socket: Socket<M>, code: Int, reason: String)
        fun onOpen(socket: Socket<M>)
    }
}