package com.maksimowiczm.zebra.core.database.di

import com.maksimowiczm.zebra.core.database.ZebraDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object DaoModule {
    @Provides
    fun provideVaultDao(database: ZebraDatabase) = database.vaultDao()
}