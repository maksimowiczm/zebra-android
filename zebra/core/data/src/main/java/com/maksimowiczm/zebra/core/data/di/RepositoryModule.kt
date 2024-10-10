@file:Suppress("unused")

package com.maksimowiczm.zebra.core.data.di

import com.maksimowiczm.zebra.core.data.api.repository.FeatureFlagRepository
import com.maksimowiczm.zebra.core.data.api.repository.FileRepository
import com.maksimowiczm.zebra.core.data.api.repository.PeerChannelRepository
import com.maksimowiczm.zebra.core.data.api.repository.SealedCredentialsRepository
import com.maksimowiczm.zebra.core.data.api.repository.UnlockRepository
import com.maksimowiczm.zebra.core.data.api.repository.UserPreferencesRepository
import com.maksimowiczm.zebra.core.data.api.repository.VaultRepository
import com.maksimowiczm.zebra.core.data.api.repository.ZebraSignalRepository
import com.maksimowiczm.zebra.core.data.repository.FeatureFlagRepositoryImpl
import com.maksimowiczm.zebra.core.data.repository.FileRepositoryImpl
import com.maksimowiczm.zebra.core.data.repository.PeerChannelRepositoryImpl
import com.maksimowiczm.zebra.core.data.repository.SealedCredentialsRepositoryImpl
import com.maksimowiczm.zebra.core.data.repository.UnlockRepositoryImpl
import com.maksimowiczm.zebra.core.data.repository.UserPreferencesRepositoryImpl
import com.maksimowiczm.zebra.core.data.repository.VaultRepositoryImpl
import com.maksimowiczm.zebra.core.data.repository.ZebraSignalRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(ViewModelComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindVaultRepository(impl: VaultRepositoryImpl): VaultRepository

    @Binds
    abstract fun bindUnlockRepository(impl: UnlockRepositoryImpl): UnlockRepository

    @Binds
    abstract fun bindFeatureFlagRepository(impl: FeatureFlagRepositoryImpl): FeatureFlagRepository

    @Binds
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository

    @Binds
    abstract fun bindPeerChannelRepository(impl: PeerChannelRepositoryImpl): PeerChannelRepository

    @Binds
    abstract fun bindSealedCredentialsRepository(impl: SealedCredentialsRepositoryImpl): SealedCredentialsRepository

    @Binds
    abstract fun bindZebraSignalRepository(impl: ZebraSignalRepositoryImpl): ZebraSignalRepository
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class UserPreferencesRepositoryModule {
    @Binds
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
