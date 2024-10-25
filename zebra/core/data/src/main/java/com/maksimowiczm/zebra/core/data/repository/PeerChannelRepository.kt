package com.maksimowiczm.zebra.core.data.repository

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.mapError
import com.maksimowiczm.zebra.core.data.model.PeerChannel
import com.maksimowiczm.zebra.core.data.model.VaultEntry
import com.maksimowiczm.zebra.core.data.model.protoPassword
import com.maksimowiczm.zebra.core.data.model.protoTitle
import com.maksimowiczm.zebra.core.data.model.protoUrl
import com.maksimowiczm.zebra.core.data.model.protoUsername
import com.maksimowiczm.zebra.core.peer.api.PeerChannel.Status
import com.maksimowiczm.zebra.core.peer.webrtc.CreateError.*
import com.maksimowiczm.zebra.core.peer.webrtc.WebRtcDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

sealed interface CreateError {
    data object PeerChannelAlreadyExists : CreateError
    data object Unknown : CreateError
}

class PeerChannelRepository @Inject constructor(
    private val webRtcDataSource: WebRtcDataSource,
    private val zebraSignalRepository: ZebraSignalRepository,
) {
    /**
     * Observe [PeerChannel] for [sessionIdentifier].
     * @return [Flow] of [PeerChannel] or null if [sessionIdentifier] is not found.
     */
    fun observePeerChannel(sessionIdentifier: String): Flow<PeerChannel?> {
        return webRtcDataSource.observePeerChannelStatus(sessionIdentifier).map {
            when (it) {
                Status.CONNECTING -> PeerChannel.Connecting
                Status.CONNECTED -> PeerChannel.Connected
                Status.CLOSED -> PeerChannel.Closed
                Status.FAILED -> PeerChannel.Failed
                Status.UNKNOWN -> null
            }
        }
    }

    fun closePeerChannel(sessionIdentifier: String) {
        webRtcDataSource.closePeerChannel(sessionIdentifier)
    }

    /**
     * Create peer channel connection.
     * @return [Result] of [Unit] or [CreateError].
     */
    suspend fun connectPeerChannel(sessionIdentifier: String): Result<Unit, CreateError> {
        val zebraSignalClient = zebraSignalRepository.getZebraSignalClient()

        return webRtcDataSource.createPeerChannelConnection(
            sessionIdentifier = sessionIdentifier,
            zebraSignalClient = zebraSignalClient,
        ).mapError {
            when (it) {
                PeerChannelAlreadyExists, PeerChannelIsNotClosed -> CreateError.PeerChannelAlreadyExists
                Unknown, is SocketError -> CreateError.Unknown
            }
        }
    }

    fun sendEntry(sessionIdentifier: String, entry: VaultEntry): Boolean {
        webRtcDataSource.sendMessage(sessionIdentifier, entry.protoTitle.toByteArray())

        entry.protoUsername?.let {
            webRtcDataSource.sendMessage(sessionIdentifier, it.toByteArray())
        }

        entry.protoPassword?.let {
            webRtcDataSource.sendMessage(sessionIdentifier, it.toByteArray())
        }

        entry.protoUrl?.let {
            webRtcDataSource.sendMessage(sessionIdentifier, it.toByteArray())
        }

        return true
    }

    /**
     * Send text message to peer.
     * @return true if message was sent successfully.
     */
    fun sendMessage(sessionIdentifier: String, message: String): Boolean {
        return webRtcDataSource.sendMessage(sessionIdentifier, message).isOk
    }

    /**
     * Send binary message to peer.
     * @return true if message was sent successfully.
     */
    fun sendMessage(sessionIdentifier: String, message: ByteArray): Boolean {
        return webRtcDataSource.sendMessage(sessionIdentifier, message).isOk
    }

    /**
     * Observe text messages from peer.
     * @return [Flow] of [String] or null if [sessionIdentifier] is not found.
     */
    fun observeMessages(sessionIdentifier: String): Flow<ByteArray>? {
        return webRtcDataSource.observeMessages(sessionIdentifier).getOrElse {
            return null
        }
    }
}