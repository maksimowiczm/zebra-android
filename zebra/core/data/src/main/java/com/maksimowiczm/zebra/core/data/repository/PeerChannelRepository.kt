package com.maksimowiczm.zebra.core.data.repository

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.mapError
import com.maksimowiczm.zebra.core.data.api.model.PeerChannel
import com.maksimowiczm.zebra.core.data.api.model.VaultEntry
import com.maksimowiczm.zebra.core.data.api.repository.CreateError
import com.maksimowiczm.zebra.core.data.api.repository.PeerChannelRepository
import com.maksimowiczm.zebra.core.data.api.repository.ZebraSignalRepository
import com.maksimowiczm.zebra.core.data.utility.protoPassword
import com.maksimowiczm.zebra.core.data.utility.protoTitle
import com.maksimowiczm.zebra.core.data.utility.protoUrl
import com.maksimowiczm.zebra.core.data.utility.protoUsername
import com.maksimowiczm.zebra.core.peer.api.PeerChannel.Status
import com.maksimowiczm.zebra.core.peer.webrtc.CreateError.*
import com.maksimowiczm.zebra.core.peer.webrtc.WebRtcDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class PeerChannelRepositoryImpl @Inject constructor(
    private val webRtcDataSource: WebRtcDataSource,
    private val zebraSignalRepository: ZebraSignalRepository,
) : PeerChannelRepository {
    /**
     * Observe [PeerChannel] for [sessionIdentifier].
     * @return [Flow] of [PeerChannel] or null if [sessionIdentifier] is not found.
     */
    override fun observePeerChannel(sessionIdentifier: String): Flow<PeerChannel?> {
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

    override fun closePeerChannel(sessionIdentifier: String) {
        webRtcDataSource.closePeerChannel(sessionIdentifier)
    }

    /**
     * Create peer channel connection.
     * @return [Result] of [Unit] or [CreateError].
     */
    override suspend fun connectPeerChannel(sessionIdentifier: String): Result<Unit, CreateError> {
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

    override fun sendEntry(sessionIdentifier: String, entry: VaultEntry): Boolean {
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
    override fun sendMessage(sessionIdentifier: String, message: String): Boolean {
        return webRtcDataSource.sendMessage(sessionIdentifier, message).isOk
    }

    /**
     * Send binary message to peer.
     * @return true if message was sent successfully.
     */
    override fun sendMessage(sessionIdentifier: String, message: ByteArray): Boolean {
        return webRtcDataSource.sendMessage(sessionIdentifier, message).isOk
    }

    /**
     * Observe text messages from peer.
     * @return [Flow] of [String] or null if [sessionIdentifier] is not found.
     */
    override fun observeMessages(sessionIdentifier: String): Flow<ByteArray>? {
        return webRtcDataSource.observeMessages(sessionIdentifier).getOrElse {
            return null
        }
    }
}