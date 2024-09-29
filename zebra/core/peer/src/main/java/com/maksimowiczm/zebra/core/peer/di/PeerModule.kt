package com.maksimowiczm.zebra.core.peer.di

import android.content.Context
import com.maksimowiczm.zebra.core.peer.webrtc.WebRtcDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object PeerModule {
    @Provides
    @Singleton
    fun providePeerChannelDataSource(
        @ApplicationContext context: Context,
    ): WebRtcDataSource {
        return WebRtcDataSource(
            context = context,
            ioDispatcher = Dispatchers.IO,
            mainDispatcher = Dispatchers.Main
        )
    }
}