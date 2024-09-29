package com.maksimowiczm.feature.share.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.zebra.core.data.model.FeatureFlag
import com.maksimowiczm.zebra.core.data.repository.FeatureFlagRepository
import com.maksimowiczm.zebra.core.domain.ObserveSignalingChannelUseCase
import com.maksimowiczm.zebra.core.domain.SetupSignalingChannelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SetupViewModel @Inject constructor(
    private val setupSignalingChannelUseCase: SetupSignalingChannelUseCase,
    observeSignalingChannelUseCase: ObserveSignalingChannelUseCase,
    private val featureFlagRepository: FeatureFlagRepository,
) : ViewModel() {
    private val _internalUiState = MutableStateFlow(InternalSetupUiState())
    val uiState = observeSignalingChannelUseCase()
        .combine(_internalUiState) { signalingServer, internalUiState ->
            if (internalUiState.isDone) {
                return@combine SetupUiState.Done
            }

            SetupUiState.Ready(
                signalingServer = internalUiState.signalingServer ?: signalingServer,
                isLoading = internalUiState.isLoading,
                isError = internalUiState.isError,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SetupUiState.Loading,
        )

    fun onInput(signalingServer: String) {
        _internalUiState.update { it.copy(signalingServer = signalingServer) }
    }

    fun onSetup() {
        viewModelScope.launch {
            _internalUiState.update { it.copy(isLoading = true) }

            val state = uiState.value
            if (state !is SetupUiState.Ready) {
                return@launch
            }

            val signalingServer =
                _internalUiState.value.signalingServer?.trim() ?: state.signalingServer

            val result = setupSignalingChannelUseCase(signalingServer)
            if (result.isErr) {
                _internalUiState.update { it.copy(isError = true, isLoading = false) }
            } else {
                featureFlagRepository.updateFeatureFlag(FeatureFlag.FEATURE_SHARE, true)
                _internalUiState.update { it.copy(isLoading = false, isDone = true) }
            }
        }
    }
}

private data class InternalSetupUiState(
    val signalingServer: String? = null,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isDone: Boolean = false,
)