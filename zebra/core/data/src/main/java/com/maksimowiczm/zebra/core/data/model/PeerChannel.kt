package com.maksimowiczm.zebra.core.data.model


sealed interface PeerChannel {
    data object Connecting : PeerChannel
    data object Connected : PeerChannel
    data object Closed : PeerChannel
    data object Failed : PeerChannel
}