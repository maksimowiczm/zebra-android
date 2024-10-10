package com.maksimowiczm.zebra.core.domain

import com.github.michaelbull.result.getOrElse
import com.maksimowiczm.zebra.core.data.api.model.PeerChannel
import com.maksimowiczm.zebra.core.data.api.repository.PeerChannelRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import com.maksimowiczm.zebra.core.data.api.repository.CreateError as RepositoryCreateError

class ConnectPeerChannelWithTimeoutUseCase @Inject constructor(
    private val peerChannelRepository: PeerChannelRepository,
) {
    sealed interface PeerChannelStatus {
        data class CreateError(val error: RepositoryCreateError) : PeerChannelStatus
        data object Connecting : PeerChannelStatus
        data object Connected : PeerChannelStatus
        data object Closed : PeerChannelStatus
        data object Failed : PeerChannelStatus
        data object ClosedWithTimeout : PeerChannelStatus
    }

    /**
     * Connects to a peer channel with a timeout.
     * If the connection is not established within the timeout, the connection is closed.
     */
    operator fun invoke(
        session: String,
        timeout: Long,
    ): Flow<PeerChannelStatus> = channelFlow {
        peerChannelRepository.connectPeerChannel(session).getOrElse {
            send(PeerChannelStatus.CreateError(it))
            return@channelFlow
        }

        send(PeerChannelStatus.Connecting)

        var timeoutReached = false

        peerChannelRepository.observePeerChannel(session).collectLatest {
            when (it) {
                is PeerChannel.Connected -> {
                    send(PeerChannelStatus.Connected)
                }

                is PeerChannel.Failed -> {
                    peerChannelRepository.closePeerChannel(session)
                    send(PeerChannelStatus.Failed)
                }

                is PeerChannel.Closed -> {
                    if (!timeoutReached) {
                        send(PeerChannelStatus.Closed)
                    }
                }

                PeerChannel.Connecting,
                null,
                -> {
                    delay(timeout)
                    timeoutReached = true
                    send(PeerChannelStatus.ClosedWithTimeout)
                    peerChannelRepository.closePeerChannel(session)
                }
            }
        }
    }
}