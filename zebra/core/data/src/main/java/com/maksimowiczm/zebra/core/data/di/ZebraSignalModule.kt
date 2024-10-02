package com.maksimowiczm.zebra.core.data.di

import com.maksimowiczm.zebra.core.zebra_signal.ZebraSignalClientFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(ViewModelComponent::class)
internal object ZebraSignalModule {

    @Provides
    fun provideZebraSignalClientFactory() = ZebraSignalClientFactory(
        ioDispatcher = Dispatchers.IO
    )
}