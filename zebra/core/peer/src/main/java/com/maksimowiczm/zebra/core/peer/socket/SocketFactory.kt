package com.maksimowiczm.zebra.core.peer.socket


interface SocketFactory<M> {
    fun create(token: String): Socket<M>
}