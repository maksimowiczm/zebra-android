package com.maksimowiczm.zebra.feature.vault.unlock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.github.michaelbull.result.getOrElse
import com.maksimowiczm.zebra.core.common.combineN
import com.maksimowiczm.zebra.core.data.model.VaultCredentials
import com.maksimowiczm.zebra.core.data.repository.UnlockError
import com.maksimowiczm.zebra.core.data.repository.UnlockRepository
import com.maksimowiczm.zebra.core.data.repository.VaultRepository
import com.maksimowiczm.zebra.core.data.model.VaultStatus
import com.maksimowiczm.zebra.feature.vault.VaultScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class UnlockVaultViewModel @Inject constructor(
    vaultRepository: VaultRepository,
    savedStateHandle: SavedStateHandle,
    private val unlockRepository: UnlockRepository,
) : ViewModel() {
    private val identifier = savedStateHandle.toRoute<VaultScreen.UnlockVaultScreen>().identifier

    private val _openingError = MutableStateFlow(false)

    val state: StateFlow<VaultUiState> = combineN(
        vaultRepository.observeVaultByIdentifier(identifier),
        unlockRepository.observeVaultStatus(identifier),
        _openingError
    ) { vault, unlockStatus, err ->
        if (vault == null || err) {
            return@combineN VaultUiState.Error
        }

        return@combineN when (unlockStatus) {
            VaultStatus.Locked -> VaultUiState.VaultFound(vault)
            VaultStatus.Unlocking -> VaultUiState.Unlocking
            is VaultStatus.Unlocked -> VaultUiState.Unlocked
            is VaultStatus.Failed -> VaultUiState.PasswordFailed
            VaultStatus.UnrecoverableError -> VaultUiState.Error
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = VaultUiState.Loading
    )

    fun onUnlock(password: String) {
        viewModelScope.launch {
            unlockRepository.unlock(
                identifier, VaultCredentials.Password(password)
            ).getOrElse {
                when (it) {
                    UnlockError.FileError,
                    UnlockError.Unknown,
                    UnlockError.VaultNotFound,
                    -> _openingError.emit(true)
                }
            }
        }
    }
}