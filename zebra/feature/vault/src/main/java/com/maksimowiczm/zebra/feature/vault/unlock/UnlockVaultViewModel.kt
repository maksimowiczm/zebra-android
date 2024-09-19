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

    private val _unrecoverableErrorFlow = MutableStateFlow(false)

    val state: StateFlow<UnlockUiState> = combineN(
        vaultRepository.observeVaultByIdentifier(identifier),
        unlockRepository.observeVaultStatus(identifier),
        _unrecoverableErrorFlow,
    ) { vault, unlockStatus, err ->
        if (vault == null || err) {
            return@combineN UnlockUiState.UnrecoverableError
        }

        return@combineN when (unlockStatus) {
            VaultStatus.Unlocking -> UnlockUiState.Unlocking(
                biometricsStatus = vault.biometricsStatus
            )

            VaultStatus.Locked -> UnlockUiState.ReadyToUnlock(
                vault = vault,
                credentialsFailed = false,
                biometricsStatus = vault.biometricsStatus,
            )

            is VaultStatus.CredentialsFailed -> UnlockUiState.ReadyToUnlock(
                vault = vault,
                credentialsFailed = true,
                biometricsStatus = vault.biometricsStatus,
            )

            is VaultStatus.Unlocked -> UnlockUiState.Unlocked

            VaultStatus.UnrecoverableError -> UnlockUiState.UnrecoverableError
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UnlockUiState.Loading
    )

    fun onUnlock(password: String) {
        viewModelScope.launch {
            unlockRepository.unlock(
                identifier = identifier,
                credentials = UnsealedVaultCredentials.Password(password)
            ).getOrElse {
                when (it) {
                    UnlockError.FileError,
                    UnlockError.Unknown,
                    UnlockError.VaultNotFound,
                    -> _unrecoverableErrorFlow.emit(true)
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

            Log.d(TAG, "Attempting to get credentials")
            val credentials = getCredentials(identifier).getOrElse {
                when (it) {
                    GetVaultCredentialsError.NotFound -> {
                        Log.d(TAG, "Vault credentials not found")
                    }

                    is GetVaultCredentialsError.Unknown -> {
                        // most likely user cancelled the biometric prompt
                        Log.d(TAG, "Vault credentials, unknown error", it.t)
                    }

                    GetVaultCredentialsError.PermanentlyInvalidated -> {
                        Log.d(TAG, "Vault credentials permanently invalidated")
                    }

                    GetVaultCredentialsError.Canceled -> {
                        Log.d(TAG, "Vault credentials canceled")
                    }
                }

                return@launch
            }

            Log.d(TAG, "Attempting to unlock vault")
            unlockRepository.unlock(identifier, credentials)
        }
    }

    fun onBiometricsInvalidatedAcknowledge() {
        viewModelScope.launch {
            deleteVaultCredentialsUseCase(identifier)
        }
    }

    companion object {
        private const val TAG = "UnlockVaultViewModel"
    }
}