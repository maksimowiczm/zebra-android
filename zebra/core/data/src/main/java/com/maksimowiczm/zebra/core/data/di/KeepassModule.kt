package com.maksimowiczm.zebra.core.data.di

import com.maksimowiczm.zebra.core.data.source.local.keepass.KeepassDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object KeepassModule {

    @Provides
    @Singleton
    fun provideKeepassDataSource(): KeepassDataSource {
        return KeepassDataSource(
            defaultDispatcher = Dispatchers.Default
        )
    }
}