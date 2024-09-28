package com.maksimowiczm.zebra.core.data.repository

import com.maksimowiczm.zebra.core.data.model.FeatureFlag
import com.maksimowiczm.zebra.core.datastore.UserPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FeatureFlagRepository @Inject constructor(
    private val userPreferencesStore: UserPreferencesDataSource,
) {
    fun observeFeatureFlag(featureFlag: FeatureFlag): Flow<Boolean> {
        return userPreferencesStore.observeFeatureFlag(
            featureFlag.name,
            featureFlag.defaultIsEnabled
        )
    }

    suspend fun updateFeatureFlag(featureFlag: FeatureFlag, isEnabled: Boolean) {
        userPreferencesStore.updateFeatureFlag(featureFlag.name, isEnabled)
    }
}