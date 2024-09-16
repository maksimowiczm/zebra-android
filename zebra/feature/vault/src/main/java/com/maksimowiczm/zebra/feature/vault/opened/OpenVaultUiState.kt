package com.maksimowiczm.zebra.feature.vault.opened

import com.maksimowiczm.zebra.core.data.model.Vault
import com.maksimowiczm.zebra.core.data.model.VaultEntry

internal sealed interface OpenVaultUiState {
    data object Loading : OpenVaultUiState
    data class Unlocked(
        val vault: Vault,
        val entries: List<VaultEntry>,
    ) : OpenVaultUiState

    data object Closed : OpenVaultUiState

    /**
     * The vault is lost. Vault is lost when the user has lost access to the vault.
     */
    data object Lost : OpenVaultUiState
}