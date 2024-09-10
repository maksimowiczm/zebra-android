package com.maksimowiczm.zebra.core.domain.di

import com.maksimowiczm.zebra.core.data.repository.VaultRepository
import com.maksimowiczm.zebra.core.domain.InsertUniqueVaultUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
internal object UseCaseModule {
    @Provides
    fun provideInsertUniqueVaultUseCase(
        vaultRepository: VaultRepository,
    ): InsertUniqueVaultUseCase {
        return InsertUniqueVaultUseCase(
            vaultRepository = vaultRepository,
            ioDispatcher = Dispatchers.IO,
        )
    }
}