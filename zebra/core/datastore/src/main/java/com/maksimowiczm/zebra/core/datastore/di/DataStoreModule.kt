package com.maksimowiczm.zebra.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.maksimowiczm.zebra.core.datastore.UserPreferencesDataSource
import com.maksimowiczm.zebra.core.datastore.UserPreferencesSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideUserPreferencesDataSource(
        @ApplicationContext context: Context,
    ): UserPreferencesDataSource {
        val dataStore = DataStoreFactory.create(
            serializer = UserPreferencesSerializer,
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            migrations = emptyList(),
            produceFile = { context.dataStoreFile("user_preferences.pb") }
        )

        return UserPreferencesDataSource(
            userPreferencesStore = dataStore
        )
    }
}