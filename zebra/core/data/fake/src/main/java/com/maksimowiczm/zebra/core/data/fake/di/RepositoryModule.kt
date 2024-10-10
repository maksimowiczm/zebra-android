package com.maksimowiczm.zebra.core.data.fake.di

import com.maksimowiczm.zebra.core.data.api.repository.FeatureFlagRepository
import com.maksimowiczm.zebra.core.data.api.repository.FileRepository
import com.maksimowiczm.zebra.core.data.api.repository.PeerChannelRepository
import com.maksimowiczm.zebra.core.data.api.repository.SealedCredentialsRepository
import com.maksimowiczm.zebra.core.data.api.repository.UnlockRepository
import com.maksimowiczm.zebra.core.data.api.repository.UserPreferencesRepository
import com.maksimowiczm.zebra.core.data.api.repository.VaultRepository
import com.maksimowiczm.zebra.core.data.api.repository.ZebraSignalRepository
import com.maksimowiczm.zebra.core.data.fake.repository.FakeFeatureFlagRepository
import com.maksimowiczm.zebra.core.data.fake.repository.FakeFileRepository
import com.maksimowiczm.zebra.core.data.fake.repository.FakePeerChannelRepository
import com.maksimowiczm.zebra.core.data.fake.repository.FakeSealedCredentialsRepository
import com.maksimowiczm.zebra.core.data.fake.repository.FakeUnlockRepository
import com.maksimowiczm.zebra.core.data.fake.repository.FakeUserPreferencesRepository
import com.maksimowiczm.zebra.core.data.fake.repository.FakeVaultRepository
import com.maksimowiczm.zebra.core.data.fake.repository.FakeZebraSignalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindVaultRepository(impl: FakeVaultRepository): VaultRepository

    @Binds
    abstract fun bindUnlockRepository(impl: FakeUnlockRepository): UnlockRepository

    @Binds
    abstract fun bindUserPreferencesRepository(impl: FakeUserPreferencesRepository): UserPreferencesRepository

    @Binds
    abstract fun bindFeatureFlagRepository(impl: FakeFeatureFlagRepository): FeatureFlagRepository

    @Binds
    abstract fun bindFileRepository(impl: FakeFileRepository): FileRepository

    @Binds
    abstract fun bindPeerChannelRepository(impl: FakePeerChannelRepository): PeerChannelRepository

    @Binds
    abstract fun bindSealedCredentialsRepository(impl: FakeSealedCredentialsRepository): SealedCredentialsRepository

    @Binds
    abstract fun bindZebraSignalRepository(impl: FakeZebraSignalRepository): ZebraSignalRepository
}