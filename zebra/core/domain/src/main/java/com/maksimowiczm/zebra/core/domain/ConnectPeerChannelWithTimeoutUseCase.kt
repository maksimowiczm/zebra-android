package com.maksimowiczm.zebra.core.domain

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.maksimowiczm.zebra.core.data.model.PeerChannel
import com.maksimowiczm.zebra.core.data.repository.PeerChannelRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ConnectPeerChannelWithTimeoutError {
    data object ConnectionTimeout : ConnectPeerChannelWithTimeoutError
    data object Unknown : ConnectPeerChannelWithTimeoutError
    data class CreateError(
        val error: com.maksimowiczm.zebra.core.data.repository.CreateError,
    ) : ConnectPeerChannelWithTimeoutError
}

class ConnectPeerChannelWithTimeoutUseCase @Inject constructor(
    private val peerChannelRepository: PeerChannelRepository,
) {
    /**
     * Connects to a peer channel with a timeout.
     * If the connection is not established within the timeout, the connection is closed.
     */
    suspend operator fun invoke(
        session: String,
        coroutineScope: CoroutineScope,
        timeout: Long,
    ): Result<Unit, ConnectPeerChannelWithTimeoutError> {
        peerChannelRepository.connectPeerChannel(session).getOrElse {
            return Err(ConnectPeerChannelWithTimeoutError.CreateError(it))
        }

        val deferred = CompletableDeferred<Result<Unit, ConnectPeerChannelWithTimeoutError>>()
        coroutineScope.launch {
            peerChannelRepository.observePeerChannel(session).collectLatest {
                when (it) {
                    is PeerChannel.Connected -> {
                        deferred.complete(Ok(Unit))
                    }

                    is PeerChannel.Failed -> {
                        peerChannelRepository.closePeerChannel(session)
                        deferred.complete(Err(ConnectPeerChannelWithTimeoutError.Unknown))
                        cancel()
                    }

                    else -> {
                        delay(timeout)
                        peerChannelRepository.closePeerChannel(session)
                        deferred.complete(Err(ConnectPeerChannelWithTimeoutError.ConnectionTimeout))
                        cancel()
                    }
                }
            }
        }

        val result = deferred.await()

        return result
    }
}