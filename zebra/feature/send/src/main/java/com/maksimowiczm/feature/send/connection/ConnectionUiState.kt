package com.maksimowiczm.feature.send.connection

internal sealed interface ConnectionUiState {
    data object Loading : ConnectionUiState
    data object Done : ConnectionUiState
    data object VaultLocked : ConnectionUiState
    data object Failed : ConnectionUiState
    data object Timeout : ConnectionUiState
}