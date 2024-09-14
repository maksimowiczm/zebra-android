package com.maksimowiczm.zebra.core.data.model


sealed interface PeerChannel {
    val sessionIdentifier: String

    data class Connecting(
        override val sessionIdentifier: String,
    ) : PeerChannel

    data class Connected(
        override val sessionIdentifier: String,
    ) : PeerChannel

    data class Closed(
        override val sessionIdentifier: String,
    ) : PeerChannel

    data class Failed(
        override val sessionIdentifier: String,
    ) : PeerChannel
}