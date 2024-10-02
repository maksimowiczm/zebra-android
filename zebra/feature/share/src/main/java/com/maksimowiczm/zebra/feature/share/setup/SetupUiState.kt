package com.maksimowiczm.zebra.feature.share.setup

import com.maksimowiczm.zebra.core.domain.SetupError

internal sealed interface SetupUiState {
    data object Loading : SetupUiState
    data class Ready(
        val signalingServer: String,
        val isLoading: Boolean,
        val error: SetupError?,
    ) : SetupUiState

    data object Done : SetupUiState
}