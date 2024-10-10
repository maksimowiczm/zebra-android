package com.maksimowiczm.zebra.feature.feature_flag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.zebra.core.data.api.model.FEATURE_FLAGS
import com.maksimowiczm.zebra.core.data.api.model.FeatureFlag
import com.maksimowiczm.zebra.core.data.api.repository.FeatureFlagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FeatureFlagViewModel @Inject constructor(
    private val featureFlagRepository: FeatureFlagRepository,
) : ViewModel() {
    val uiState = FEATURE_FLAGS
        .map { featureFlag ->
            featureFlagRepository.observeFeatureFlag(featureFlag)
                .map { isEnabled -> featureFlag to isEnabled }
        }
        .let { flows -> combine(flows) { it.toMap() } }
        .map { featureFlags ->
            FeatureFlagUiState(
                isLoading = false,
                featureFlags = featureFlags,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FeatureFlagUiState(),
        )

    fun updateFeatureFlag(featureFlag: FeatureFlag, isEnabled: Boolean) {
        viewModelScope.launch {
            featureFlagRepository.updateFeatureFlag(featureFlag, isEnabled)
        }
    }
}