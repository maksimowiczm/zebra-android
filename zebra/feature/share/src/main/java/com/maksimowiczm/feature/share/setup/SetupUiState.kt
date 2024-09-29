package com.maksimowiczm.feature.share.setup

internal sealed interface SetupUiState {
    data object Loading : SetupUiState
    data class Ready(
        val signalingServer: String,
        val isLoading: Boolean,
        val isError: Boolean,
    ) : SetupUiState

    data object Done : SetupUiState
}