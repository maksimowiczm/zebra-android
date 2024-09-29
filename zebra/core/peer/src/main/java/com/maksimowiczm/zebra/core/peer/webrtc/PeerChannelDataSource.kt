package com.maksimowiczm.zebra.core.peer.webrtc

import android.content.Context
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.maksimowiczm.zebra.core.peer.api.PeerChannel
import kotlinx.coroutines.flow.Flow
import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.peer.socket.ZebraWebRTCOkHttpSocketFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ObserveError {
    data object PeerChannelNotFound : ObserveError
}

sealed interface CreateError {
    data object PeerChannelAlreadyExists : CreateError
    data object PeerChannelIsNotClosed : CreateError
    data object Unknown : CreateError
}

sealed interface SendMessageError {
    data object PeerChannelNotFound : SendMessageError
    data object Unknown : SendMessageError
}

class WebRtcDataSource(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher,
    mainDispatcher: CoroutineDispatcher,
) {
    private val peerStatus = mutableMapOf<String, MutableStateFlow<PeerChannel.Status>>()
    private val peerChannels = mutableMapOf<String, Pair<PeerChannel, PeerChannelListener>>()
    private val mainScope = CoroutineScope(mainDispatcher)

    fun observePeerChannelStatus(sessionIdentifier: String): Flow<PeerChannel.Status> {
        val flow = peerStatus[sessionIdentifier]

        if (flow != null) {
            return flow
        }

        peerStatus[sessionIdentifier] = MutableStateFlow(PeerChannel.Status.UNKNOWN)

        return peerStatus[sessionIdentifier]!!
    }

    fun sendMessage(sessionIdentifier: String, message: String): Result<Unit, SendMessageError> {
        val (peerChannel, _) = peerChannels[sessionIdentifier]
            ?: return Err(SendMessageError.PeerChannelNotFound)

        val result = peerChannel.send(message)

        return if (result) {
            Ok(Unit)
        } else {
            Err(SendMessageError.Unknown)
        }
    }

    fun sendMessage(sessionIdentifier: String, message: ByteArray): Result<Unit, SendMessageError> {
        val (peerChannel, _) = peerChannels[sessionIdentifier]
            ?: return Err(SendMessageError.PeerChannelNotFound)

        val result = peerChannel.send(message)

        return if (result) {
            Ok(Unit)
        } else {
            Err(SendMessageError.Unknown)
        }
    }

    fun observeMessages(sessionIdentifier: String): Result<Flow<ByteArray>, ObserveError> {
        val (_, listener) = peerChannels[sessionIdentifier]
            ?: return Err(ObserveError.PeerChannelNotFound)

        return Ok(listener.messageFlow)
    }

    private fun canCreatePeerChannel(sessionIdentifier: String): Result<Unit, CreateError> {
        // If channel exists, it's an error
        if (peerChannels[sessionIdentifier] != null) {
            return Err(CreateError.PeerChannelAlreadyExists)
        }

        val status = peerStatus[sessionIdentifier]?.value

        return when (status) {
            // If channel doesn't exist, it's ok
            PeerChannel.Status.UNKNOWN, null -> Ok(Unit)
            // If channel is closed or failed, it can be recreated
            PeerChannel.Status.CLOSED, PeerChannel.Status.FAILED -> Ok(Unit)
            // If channel is connecting or connected, it's an error
            PeerChannel.Status.CONNECTING, PeerChannel.Status.CONNECTED -> Err(CreateError.PeerChannelIsNotClosed)
        }
    }

    suspend fun createPeerChannelConnection(
        sessionIdentifier: String,
        signalingChannelUrl: String,
    ): Result<Unit, CreateError> {
        val canCreateResult = canCreatePeerChannel(sessionIdentifier)
        if (canCreateResult.isErr) {
            return canCreateResult
        }

        val status = peerStatus[sessionIdentifier]
        if (status != null) {
            status.emit(PeerChannel.Status.CONNECTING)
        } else {
            peerStatus[sessionIdentifier] = MutableStateFlow(PeerChannel.Status.CONNECTING)
        }

        val builder = WebRtcPeerChannelBuilder(
            context = context,
            ioDispatcher = ioDispatcher,
            socketFactory = ZebraWebRTCOkHttpSocketFactory(signalingChannelUrl),
        )

        val listener = PeerChannelListener(sessionIdentifier)
        val peerChannel = builder.build(sessionIdentifier, listener)

        // If channel creation failed, update status and return error
        if (peerChannel == null) {
            peerStatus[sessionIdentifier]!!.update { PeerChannel.Status.FAILED }
            return Err(CreateError.Unknown)
        }

        // If channel creation succeeded store channel and listener
        peerChannels[sessionIdentifier] = peerChannel to listener

        return Ok(Unit)
    }

    fun closePeerChannel(sessionIdentifier: String) {
        // let closed channels statuses live forever
        // but garbage collect channel object
        peerChannels[sessionIdentifier]?.first?.close()
        peerChannels.remove(sessionIdentifier)
    }

    // todo investigate possible race conditions
    private inner class PeerChannelListener(
        private val identifier: String,
    ) : PeerChannel.Listener {
        val messageFlow = MutableSharedFlow<ByteArray>()

        override fun onMessage(channel: PeerChannel, message: ByteArray) {
            mainScope.launch {
                messageFlow.emit(message)
            }
        }

        override fun onOpen(channel: PeerChannel) {
            mainScope.launch {
                // Some breathing space for channels
                delay(200)
                peerStatus[identifier]?.update { PeerChannel.Status.CONNECTED }
            }
        }

        override fun onClosed(channel: PeerChannel) {
            peerStatus[identifier]?.update { PeerChannel.Status.CLOSED }
        }

        override fun onFailure(channel: PeerChannel?, cause: Throwable) {
            peerStatus[identifier]?.update { PeerChannel.Status.FAILED }
        }
    }
}