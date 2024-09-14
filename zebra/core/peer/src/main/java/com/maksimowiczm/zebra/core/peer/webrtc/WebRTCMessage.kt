package com.maksimowiczm.zebra.core.peer.webrtc

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.Serializable

@Serializable
sealed interface WebRTCMessage {
    @Serializable
    data class Answer(val sdp: String) : WebRTCMessage

    @Serializable
    data class IceCandidate(
        val candidate: String,
        val sdpMLineIndex: Int,
        val sdpMid: String,
    ) : WebRTCMessage

    @Serializable
    data class Offer(val sdp: String) : WebRTCMessage
}

internal class WebRTCMessageSerializer : KSerializer<WebRTCMessage> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("WebRTCMessage") {
        element<String>("type")
        element<String>("sdp")
        element<String>("sdpMid")
        element<Int>("sdpMLineIndex")
        element<String>("candidate")
    }

    companion object {
        const val TYPE = 0
        const val SDP = 1
        const val SDP_MID = 2
        const val SDP_M_LINE_INDEX = 3
        const val CANDIDATE = 4
    }

    override fun serialize(encoder: Encoder, value: WebRTCMessage) = when (value) {
        is WebRTCMessage.Answer -> encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, TYPE, "answer")
            encodeStringElement(descriptor, SDP, value.sdp)
        }

        is WebRTCMessage.Offer -> encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, TYPE, "offer")
            encodeStringElement(descriptor, SDP, value.sdp)
        }

        is WebRTCMessage.IceCandidate -> encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, CANDIDATE, value.candidate)
            encodeStringElement(descriptor, SDP_MID, value.sdpMid)
            encodeIntElement(descriptor, SDP_M_LINE_INDEX, value.sdpMLineIndex)
        }
    }

    override fun deserialize(decoder: Decoder): WebRTCMessage {
        return decoder.decodeStructure(descriptor) {
            var type: String? = null
            var sdp: String? = null
            var sdpMid: String? = null
            var sdpMLineIndex: Int? = null
            var candidate: String? = null

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    TYPE -> type = decodeStringElement(descriptor, index)
                    SDP -> sdp = decodeStringElement(descriptor, index)
                    SDP_MID -> sdpMid = decodeStringElement(descriptor, index)
                    SDP_M_LINE_INDEX -> sdpMLineIndex = decodeIntElement(descriptor, index)
                    CANDIDATE -> candidate = decodeStringElement(descriptor, index)
                    else -> throw SerializationException("Unknown index: $index")
                }
            }

            when (type) {
                "answer" -> WebRTCMessage.Answer(sdp!!)
                "offer" -> WebRTCMessage.Offer(sdp!!)
                else -> WebRTCMessage.IceCandidate(candidate!!, sdpMLineIndex!!, sdpMid!!)
            }
        }
    }
}
