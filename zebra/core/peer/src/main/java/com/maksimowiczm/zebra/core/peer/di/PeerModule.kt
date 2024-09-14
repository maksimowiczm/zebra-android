package com.maksimowiczm.zebra.core.peer.di

import android.content.Context
import com.maksimowiczm.zebra.core.peer.BuildConfig
import com.maksimowiczm.zebra.core.peer.socket.Socket
import com.maksimowiczm.zebra.core.peer.socket.SocketFactory
import com.maksimowiczm.zebra.core.peer.webrtc.WebRTCMessage
import com.maksimowiczm.zebra.core.peer.webrtc.WebRTCMessageSerializer
import com.maksimowiczm.zebra.core.peer.webrtc.WebRTCSocket
import com.maksimowiczm.zebra.core.peer.webrtc.WebRtcDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object PeerModule {
    private const val SOCKET = BuildConfig.WS_SOCKET

    @Provides
    @Singleton
    fun providePeerChannelDataSource(
        @ApplicationContext context: Context,
    ): WebRtcDataSource {
        return WebRtcDataSource(
            context = context,
            ioDispatcher = Dispatchers.IO,
            mainDispatcher = Dispatchers.Main,
            socketFactory = object : SocketFactory<WebRTCMessage> {
                override fun create(token: String): Socket<WebRTCMessage> {
                    return WebRTCSocket(
                        {
                            OkHttpClient()
                                .newWebSocket(
                                    Request.Builder().url("$SOCKET?token=$token").build(),
                                    it
                                )
                        },
                        WebRTCMessageSerializer()
                    )
                }
            }
        )
    }
}