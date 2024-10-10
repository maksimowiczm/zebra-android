package com.maksimowiczm.zebra.core.data.api.repository

import com.maksimowiczm.zebra.core.data.api.model.FeatureFlag
import kotlinx.coroutines.flow.Flow

interface FeatureFlagRepository {
    fun observeFeatureFlag(featureFlag: FeatureFlag): Flow<Boolean>
    suspend fun updateFeatureFlag(featureFlag: FeatureFlag, isEnabled: Boolean)
}