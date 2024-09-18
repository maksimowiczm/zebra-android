package com.maksimowiczm.zebra.feature.vault.biometrics

sealed interface BiometricsUiState {
    data object Setup : BiometricsUiState
    data object Success : BiometricsUiState
    data object Loading : BiometricsUiState
    data object Failed : BiometricsUiState
    data object Canceled : BiometricsUiState
}