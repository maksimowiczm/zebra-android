package com.maksimowiczm.zebra.core.peer.webrtc

import android.content.Context
import android.util.Log
import com.maksimowiczm.zebra.core.peer.api.PeerChannel
import com.maksimowiczm.zebra.core.peer.socket.Socket
import com.maksimowiczm.zebra.core.peer.socket.SocketFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

internal class WebRtcPeerChannelBuilder(
    context: Context,
    private val socketFactory: SocketFactory<WebRTCMessage>,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val factory by lazy {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(context)
                .createInitializationOptions()
        )

        PeerConnectionFactory.builder().createPeerConnectionFactory()
    }

    fun build(
        session: String,
        listener: PeerChannel.Listener,
    ): PeerChannel? {
        val signalingChannel = socketFactory.create(token = session)

        val builder = DataChannelBuilder(
            signalingChannel = signalingChannel,
            listener = listener,
            ioDispatcher = ioDispatcher,
        )

        Log.d(TAG, "Creating peer connection")
        val connection = factory.createPeerConnection(defaultRtcConfig, builder)
            ?: return null

        val dataChannelInit = DataChannel.Init().apply {
            ordered = true
        }

        val channel = builder.offer(
            connection = connection,
            dataChannel = connection.createDataChannel(DATA_CHANNEL_NAME, dataChannelInit)
        )

        return channel
    }

    companion object {
        private const val TAG = "WebRtcPeerChannelBuilder"

        // not required to be unique https://developer.mozilla.org/en-US/docs/Web/API/RTCDataChannel/label
        private const val DATA_CHANNEL_NAME = "ZEBRA"
        val defaultRtcConfig by lazy {
            PeerConnection.RTCConfiguration(
                arrayListOf(
                    PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
                        .createIceServer()
                )
            )
        }
    }
}

class UnexpectedMessageException(message: String) : Exception(message)
class SdpObserverException(message: String) : Exception(message)
class SignalingChannelException(message: String) : Exception(message)

//<editor-fold desc="InnerConnectionBuilder">
private class DataChannelBuilder(
    private val signalingChannel: Socket<WebRTCMessage>,
    private val listener: PeerChannel.Listener,
    ioDispatcher: CoroutineDispatcher,
) : PeerConnection.Observer {
    private var dataChannelWrapper: DataChannelWrapper? = null
    private val connectingJob = Job().apply { invokeOnCompletion { Log.d(TAG, "Job completed") } }
    private val connectingScope = CoroutineScope(ioDispatcher + connectingJob)

    private val messageFlow = callbackFlow {
        val listener = object : Socket.Listener<WebRTCMessage> {
            override fun onMessage(socket: Socket<WebRTCMessage>, message: WebRTCMessage) {
                connectingScope.launch {
                    send(message)
                }
            }

            override fun onFailure(socket: Socket<WebRTCMessage>, t: Throwable) {
                // If signaling channel fails before establishing connection with peer cancel the connection
                connectingJob.cancel()
                listener.onFailure(null, t)
                close()
            }

            override fun onClosed(socket: Socket<WebRTCMessage>, code: Int, reason: String) {
                // the connection may or may NOT be established when signaling channel is closed
                // we don't know, we don't care
                close()
            }

            override fun onOpen(socket: Socket<WebRTCMessage>) = Unit
        }

        signalingChannel.addListener(listener)

        awaitClose {
            signalingChannel.removeListener(listener)
        }
    }

    fun offer(connection: PeerConnection, dataChannel: DataChannel): DataChannelWrapper {
        val dataChannelWrapper = DataChannelWrapper(dataChannel, connection, listener)
        this.dataChannelWrapper = dataChannelWrapper

        connectingScope.launch {
            messageFlow.collect { message ->
                when (message) {
                    is WebRTCMessage.Answer -> {
                        Log.i(TAG, "Received answer")
                        connection.setRemoteDescription(object : SdpObserver {
                            override fun onCreateSuccess(sessionDescription: SessionDescription) {}
                            override fun onSetSuccess() {}
                            override fun onCreateFailure(p0: String) {
                                listener.onFailure(dataChannelWrapper, SdpObserverException(p0))
                                connectingJob.cancel(p0)
                            }

                            override fun onSetFailure(p0: String) {
                                listener.onFailure(dataChannelWrapper, SdpObserverException(p0))
                                connectingJob.cancel(p0)
                            }
                        }, message.toSessionDescription())
                    }

                    is WebRTCMessage.IceCandidate -> {
                        Log.i(TAG, "Received ice candidate")
                        if (!connection.addIceCandidate(message.toIceCandidate())) {
                            Log.w(TAG, "Failed to add ice candidate")
                            connectingJob.cancel()
                        }
                    }

                    // ignore offer messages
                    is WebRTCMessage.Offer -> {
                        Log.i(TAG, "Received offer message")
                        Log.e(TAG, "Unexpected offer message, closing data channel and connection")
                        listener.onFailure(
                            dataChannelWrapper,
                            UnexpectedMessageException("Unexpected offer message")
                        )
                        connectingJob.cancel()
                    }
                }
            }
        }

        Log.i(TAG, "Creating offer")
        connection.createOffer(
            object : SdpObserver {
                override fun onSetSuccess() {}
                override fun onCreateFailure(p0: String) {
                    Log.e(TAG, "Failed to create offer")
                    listener.onFailure(dataChannelWrapper, SdpObserverException(p0))
                    connectingJob.cancel(p0)
                }

                override fun onSetFailure(p0: String) {
                    Log.e(TAG, "Failed to set offer")
                    listener.onFailure(dataChannelWrapper, SdpObserverException(p0))
                    connectingJob.cancel(p0)
                }

                override fun onCreateSuccess(sessionDescription: SessionDescription) {
                    connection.setLocalDescription(this, sessionDescription)
                    if (!signalingChannel.send(sessionDescription.toMessage().getOrThrow())) {
                        Log.e(TAG, "Failed to send offer")
                        listener.onFailure(
                            dataChannelWrapper,
                            SignalingChannelException("Failed to send offer")
                        )
                        connectingJob.cancel()
                    }
                }
            },
            MediaConstraints()
        )

        return dataChannelWrapper
    }

    override fun onSignalingChange(state: PeerConnection.SignalingState) {
        Log.d(TAG, "Signaling state changed to: $state")

        if (state == PeerConnection.SignalingState.STABLE) {
            Log.i(TAG, "Signaling state is stable")
            // connection is established, don't need connecting scope anymore
            connectingJob.cancel()
            // don't need signaling channel anymore
            signalingChannel.close(1000, null)
        }

        if (state == PeerConnection.SignalingState.CLOSED) {
            Log.i(TAG, "Signaling state is closed")
            if (dataChannelWrapper != null) {
                listener.onClosed(dataChannelWrapper!!)
            }

            connectingJob.cancel()
            signalingChannel.close(1000, null)
        }
    }

    override fun onIceCandidate(candidate: IceCandidate) {
        Log.d(TAG, "Sending ice candidate: $candidate")
        if (!signalingChannel.send(candidate.toMessage())) {
            Log.e(TAG, "Failed to send ice candidate")
        }
    }

    override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) = Unit
    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) = Unit
    override fun onIceConnectionReceivingChange(p0: Boolean) = Unit
    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) = Unit
    override fun onAddStream(p0: MediaStream?) = Unit
    override fun onRemoveStream(p0: MediaStream?) = Unit
    override fun onDataChannel(p0: DataChannel?) = Unit
    override fun onRenegotiationNeeded() = Unit

    companion object {
        private const val TAG = "DataChannelBuilder"
    }
}
//</editor-fold>


/**
 * Wraps webrtc data channel.
 */
private class DataChannelWrapper(
    private val channel: DataChannel,
    private val connection: PeerConnection,
    listener: PeerChannel.Listener,
) : PeerChannel {
    // might cause race conditions
    private val isClosed = AtomicBoolean(false)

    init {
        Log.d(TAG, "Registering data channel observer")
        channel.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(p0: Long) {}
            override fun onStateChange() {
                Log.d(TAG, "Data channel state changed to: ${channel.state()}")
                if (channel.state() == DataChannel.State.OPEN) {
                    listener.onOpen(this@DataChannelWrapper)
                }

                if (channel.state() == DataChannel.State.CLOSED) {
                    isClosed.set(true)
                    listener.onClosed(this@DataChannelWrapper)
                }
            }

            override fun onMessage(buffer: DataChannel.Buffer) {
                Log.d(TAG, "Received message")
                val bytes = ByteArray(buffer.data.remaining())
                buffer.data.get(bytes)
                listener.onMessage(this@DataChannelWrapper, bytes)
            }
        })
    }

    override fun send(message: String): Boolean {
        Log.d(TAG, "Sending message")
        val buffer = DataChannel.Buffer(ByteBuffer.wrap(message.toByteArray()), false)
        return channel.send(buffer)
    }

    override fun send(message: ByteArray): Boolean {
        Log.d(TAG, "Sending message")
        val buffer = DataChannel.Buffer(ByteBuffer.wrap(message), true)
        return channel.send(buffer)
    }

    override fun close() {
        // You are allowed to close the channel only once
        if (isClosed.compareAndSet(false, true)) {
            Log.i(TAG, "Closing channel on request")
            channel.unregisterObserver()
            channel.close()
            connection.close()
        } else {
            Log.w(TAG, "Channel is already closed")
        }
    }

    companion object {
        private const val TAG = "DataChannelWrapper"
    }
}