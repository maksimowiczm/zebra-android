package com.maksimowiczm.zebra.core.data.fake.repository

import com.maksimowiczm.zebra.core.data.api.model.FeatureFlag
import com.maksimowiczm.zebra.core.data.api.repository.FeatureFlagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FakeFeatureFlagRepository @Inject constructor() : FeatureFlagRepository {
    override fun observeFeatureFlag(featureFlag: FeatureFlag): Flow<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun updateFeatureFlag(
        featureFlag: FeatureFlag,
        isEnabled: Boolean,
    ) {
        TODO("Not yet implemented")
    }
}