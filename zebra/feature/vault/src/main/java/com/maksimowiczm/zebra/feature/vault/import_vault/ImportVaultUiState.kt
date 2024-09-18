package com.maksimowiczm.zebra.feature.vault.import_vault

import android.net.Uri
import com.maksimowiczm.zebra.core.data.model.Vault


internal sealed interface ImportVaultUiState {
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
        val path: Uri,
    ) : ImportVaultUiState

    /**
     * File was added.
     */
    data class Done(val vault: Vault) : ImportVaultUiState

    data object FileImportError : ImportVaultUiState

    data class VaultExists(val vault: Vault) : ImportVaultUiState
}