package com.maksimowiczm.zebra.feature.vault.unlock

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.github.michaelbull.result.getOrElse
import com.maksimowiczm.zebra.core.biometry.BiometricManager
import com.maksimowiczm.zebra.core.common.combineN
import com.maksimowiczm.zebra.core.data.model.UnsealedVaultCredentials
import com.maksimowiczm.zebra.core.data.model.VaultStatus
import com.maksimowiczm.zebra.core.data.repository.SealedCredentialsRepository
import com.maksimowiczm.zebra.core.data.repository.UnlockError
import com.maksimowiczm.zebra.core.data.repository.UnlockRepository
import com.maksimowiczm.zebra.core.data.repository.VaultRepository
import com.maksimowiczm.zebra.core.domain.DeleteVaultCredentialsUseCase
import com.maksimowiczm.zebra.core.domain.GetVaultCredentialsError
import com.maksimowiczm.zebra.core.domain.GetVaultCredentialsUseCase
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
    private val credentialsRepository: SealedCredentialsRepository,
    private val deleteVaultCredentialsUseCase: DeleteVaultCredentialsUseCase,
) : ViewModel() {
    private val identifier = savedStateHandle.toRoute<VaultScreen.UnlockVaultScreen>().identifier

    private val _openingError = MutableStateFlow(false)
    private val _biometricsInvalidated = MutableStateFlow(false)

    private val hasCredentials = credentialsRepository
        .observeCredentialsAvailable(vaultIdentifier = identifier)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val state: StateFlow<VaultUiState> = combineN(
        vaultRepository.observeVaultByIdentifier(identifier),
        unlockRepository.observeVaultStatus(identifier),
        _openingError,
        _biometricsInvalidated
    ) { vault, unlockStatus, err, biometricsError ->
        if (vault == null || err) {
            return@combineN VaultUiState.Error
        }

        return@combineN when (unlockStatus) {
            VaultStatus.Locked -> VaultUiState.VaultFound(vault, biometricsError)
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
                identifier, UnsealedVaultCredentials.Password(password)
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

    fun tryUnlock(
        biometricManager: BiometricManager,
    ) {
        viewModelScope.launch {
            val status = unlockRepository.getVaultStatus(identifier)
            if (status is VaultStatus.Unlocked) {
                return@launch
            }

            val getCredentials = GetVaultCredentialsUseCase(
                cryptoContext = biometricManager.cryptoContext,
                credentialsRepository = credentialsRepository
            )

            val credentials = getCredentials(identifier).getOrElse {
                when (it) {
                    GetVaultCredentialsError.NotFound -> {
                        Log.d(TAG, "Vault credentials not found")
                    }

                    is GetVaultCredentialsError.Unknown -> {
                        // most likely user cancelled the biometric prompt
                    }

                    GetVaultCredentialsError.PermanentlyInvalidated -> {
                        deleteVaultCredentialsUseCase(identifier)
                        _biometricsInvalidated.emit(true)
                    }
                }

                return@launch
            }

            unlockRepository.unlock(identifier, credentials)
        }
    }

    /**
     * Attempt to unlock the vault using biometrics.
     * @return true if the vault can be unlocked, false otherwise.
     */
    fun onBiometrics(biometricManager: BiometricManager): Boolean {
        if (hasCredentials.value && !_openingError.value) {
            tryUnlock(biometricManager)
            return true
        }

        return false
    }

    fun onBiometricsInvalidatedAcknowledge() {
        viewModelScope.launch {
            _biometricsInvalidated.emit(false)
            deleteVaultCredentialsUseCase(identifier)
        }
    }

    companion object {
        private const val TAG = "UnlockVaultViewModel"
    }
}