package com.maksimowiczm.zebra.feature.vault.import_vault


sealed interface ImportVaultUiState {
    /**
     * Literally nothing is happening.
     */
    data object Idle : ImportVaultUiState

    /**
     * Loading something.
     */
    data object Loading : ImportVaultUiState

    /**
     * User must pick a vault file.
     */
    data object PickFile : ImportVaultUiState

    /**
     * User canceled file picking.
     */
    data object PickFileCanceled : ImportVaultUiState

    /**
     * File is ready to be added.
     */
    data class FileReady(
        val name: String,
    ) : ImportVaultUiState

    /**
     * File was added.
     */
    data object Done : ImportVaultUiState

    data object FileError : ImportVaultUiState
}