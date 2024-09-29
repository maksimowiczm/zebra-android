package com.maksimowiczm.zebra.core.peer.socket

import com.maksimowiczm.zebra.core.peer.webrtc.WebRTCMessage
import com.maksimowiczm.zebra.core.peer.webrtc.WebRTCMessageSerializer
import com.maksimowiczm.zebra.core.peer.webrtc.WebRTCSocket
import okhttp3.OkHttpClient
import okhttp3.Request

internal class ZebraWebRTCOkHttpSocketFactory(
    private val url: String,
) : SocketFactory<WebRTCMessage> {
    override fun create(token: String): Socket<WebRTCMessage> {
        return WebRTCSocket(
            {
                OkHttpClient()
                    .newWebSocket(
                        Request.Builder().url("$url?token=$token").build(),
                        it
                    )
            },
            WebRTCMessageSerializer()
        )
    }
}