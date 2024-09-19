package com.maksimowiczm.zebra.feature.vault.opened

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.maksimowiczm.zebra.core.clipboard.ClipboardManager
import com.maksimowiczm.zebra.core.data.model.VaultStatus
import com.maksimowiczm.zebra.core.data.repository.UnlockRepository
import com.maksimowiczm.zebra.core.data.repository.VaultRepository
import com.maksimowiczm.zebra.feature.vault.VaultScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
internal class OpenedVaultViewModel @Inject constructor(
    vaultRepository: VaultRepository,
    private val unlockRepository: UnlockRepository,
    savedStateHandle: SavedStateHandle,
    private val clipboardManager: ClipboardManager,
) : ViewModel() {
    private val identifier = savedStateHandle.toRoute<VaultScreen.OpenedVaultScreen>().identifier

    val state: StateFlow<OpenVaultUiState> =
        unlockRepository.observeVaultStatus(identifier).map { status ->
            when (status) {
                VaultStatus.Locked -> OpenVaultUiState.Closed
                VaultStatus.Unlocking -> OpenVaultUiState.Loading
                is VaultStatus.CredentialsFailed -> OpenVaultUiState.Closed
                VaultStatus.UnrecoverableError -> OpenVaultUiState.Lost
                is VaultStatus.Unlocked -> {
                    val vault = vaultRepository.getVaultByIdentifier(identifier)
                        ?: return@map OpenVaultUiState.Lost

                    OpenVaultUiState.Unlocked(
                        vault = vault,
                        entries = status.entries
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OpenVaultUiState.Loading
        )

    fun onLock() {
        unlockRepository.lock(identifier)
    }

    fun onCopy(content: String, confidential: Boolean) {
        clipboardManager.copy(content, confidential)
    }
}