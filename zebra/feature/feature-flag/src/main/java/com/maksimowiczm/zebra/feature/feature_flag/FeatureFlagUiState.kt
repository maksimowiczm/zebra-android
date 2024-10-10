package com.maksimowiczm.zebra.feature.feature_flag

import com.maksimowiczm.zebra.core.data.api.model.FeatureFlag

internal data class FeatureFlagUiState(
    val isLoading: Boolean = true,
    val featureFlags: Map<FeatureFlag, Boolean> = emptyMap(),
)