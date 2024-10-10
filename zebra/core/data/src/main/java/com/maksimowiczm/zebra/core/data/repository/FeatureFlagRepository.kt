package com.maksimowiczm.zebra.core.data.repository

import com.maksimowiczm.zebra.core.data.api.model.FeatureFlag
import com.maksimowiczm.zebra.core.data.api.repository.FeatureFlagRepository
import com.maksimowiczm.zebra.core.datastore.UserPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FeatureFlagRepositoryImpl @Inject constructor(
    private val userPreferencesStore: UserPreferencesDataSource,
) : FeatureFlagRepository {
    override fun observeFeatureFlag(featureFlag: FeatureFlag): Flow<Boolean> {
        return userPreferencesStore.observeFeatureFlag(
            featureFlag.name,
            featureFlag.defaultIsEnabled
        )
    }

    override suspend fun updateFeatureFlag(featureFlag: FeatureFlag, isEnabled: Boolean) {
        userPreferencesStore.updateFeatureFlag(featureFlag.name, isEnabled)
    }
}