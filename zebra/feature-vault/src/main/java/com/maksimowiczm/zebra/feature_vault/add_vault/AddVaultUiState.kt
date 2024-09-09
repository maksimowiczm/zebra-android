package com.maksimowiczm.zebra.feature_vault.add_vault


sealed interface AddVaultUiState {
    /**
     * Literally nothing is happening.
     */
    data object Idle : AddVaultUiState

    /**
     * Loading something.
     */
    data object Loading : AddVaultUiState

    /**
     * User must pick a vault file.
     */
    data object PickFile : AddVaultUiState

    /**
     * User canceled file picking.
     */
    data object PickFileCanceled : AddVaultUiState

    /**
     * File is ready to be added.
     */
    data class FileReady(
        val name: String,
    ) : AddVaultUiState

    /**
     * File was added.
     */
    data object Done : AddVaultUiState
}