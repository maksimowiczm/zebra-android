package com.maksimowiczm.zebra.core.database.di

import android.content.Context
import androidx.room.Room
import com.maksimowiczm.zebra.core.database.ZebraDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        ZebraDatabase::class.java,
        "zebra-database"
    ).build()
}