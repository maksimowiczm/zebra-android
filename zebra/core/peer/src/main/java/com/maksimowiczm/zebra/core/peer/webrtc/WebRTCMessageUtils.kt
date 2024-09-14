package com.maksimowiczm.zebra.core.peer.webrtc

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

internal fun WebRTCMessage.Answer.toSessionDescription() =
    SessionDescription(SessionDescription.Type.ANSWER, sdp)

internal fun WebRTCMessage.IceCandidate.toIceCandidate() = IceCandidate(candidate, sdpMLineIndex, sdpMid)

internal fun IceCandidate.toMessage() = WebRTCMessage.IceCandidate(sdp, sdpMLineIndex, sdpMid)

internal fun SessionDescription.toMessage() = when (type) {
    SessionDescription.Type.ANSWER -> Result.success(WebRTCMessage.Answer(description))
    SessionDescription.Type.OFFER -> Result.success(WebRTCMessage.Offer(description))
    else -> Result.failure(IllegalArgumentException("Unsupported session description type: $type"))
}