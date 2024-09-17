package com.maksimowiczm.zebra.feature.vault.unlock

import com.maksimowiczm.zebra.core.data.model.Vault

internal sealed interface VaultUiState {
    data object Loading : VaultUiState
    data object Error : VaultUiState
    data class VaultFound(
        val vault: Vault,
        val biometricsInvalidated: Boolean,
    ) : VaultUiState

    data object Unlocked : VaultUiState
    data object PasswordFailed : VaultUiState
    data object Unlocking : VaultUiState
}