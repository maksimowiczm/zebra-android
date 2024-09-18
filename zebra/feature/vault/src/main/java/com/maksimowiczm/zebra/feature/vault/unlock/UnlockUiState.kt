package com.maksimowiczm.zebra.feature.vault.unlock

import com.maksimowiczm.zebra.core.data.model.Vault
import com.maksimowiczm.zebra.core.data.model.VaultBiometricsStatus

sealed interface UnlockUiState {
    data object Loading : UnlockUiState
    data class ReadyToUnlock(
        val vault: Vault,
        val credentialsFailed: Boolean,
        val biometricsStatus: VaultBiometricsStatus,
    ) : UnlockUiState

    data class Unlocking(
        val biometricsStatus: VaultBiometricsStatus,
    ) : UnlockUiState

    data object UnrecoverableError : UnlockUiState
    data object Unlocked : UnlockUiState
}