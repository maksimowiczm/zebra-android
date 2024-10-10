package com.maksimowiczm.zebra.core.data.fake.repository

import com.maksimowiczm.zebra.core.data.api.model.FeatureFlag
import com.maksimowiczm.zebra.core.data.api.repository.FeatureFlagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

var shareFlag = MutableStateFlow(true)

class FakeFeatureFlagRepository @Inject constructor() : FeatureFlagRepository {

    override fun observeFeatureFlag(featureFlag: FeatureFlag): Flow<Boolean> {
        return when (featureFlag) {
            FeatureFlag.FEATURE_SHARE -> shareFlag
        }
    }

    override suspend fun updateFeatureFlag(
        featureFlag: FeatureFlag,
        isEnabled: Boolean,
    ) {
        when (featureFlag) {
            FeatureFlag.FEATURE_SHARE -> shareFlag.emit(isEnabled)
        }
    }
}