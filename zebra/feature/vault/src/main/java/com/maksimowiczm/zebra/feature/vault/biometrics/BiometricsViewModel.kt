package com.maksimowiczm.zebra.feature.vault.biometrics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.github.michaelbull.result.getOrElse
import com.maksimowiczm.zebra.core.biometry.BiometricManager
import com.maksimowiczm.zebra.core.data.model.UnsealedVaultCredentials
import com.maksimowiczm.zebra.core.data.model.VaultStatus
import com.maksimowiczm.zebra.core.data.repository.SealedCredentialsRepository
import com.maksimowiczm.zebra.core.data.repository.UnlockRepository
import com.maksimowiczm.zebra.core.domain.AddVaultCredentialsError
import com.maksimowiczm.zebra.core.domain.AddVaultCredentialsUseCase
import com.maksimowiczm.zebra.feature.vault.VaultScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BiometricsViewModel @Inject constructor(
    private val credentialsRepository: SealedCredentialsRepository,
    private val unlockRepository: UnlockRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val identifier = savedStateHandle.toRoute<VaultScreen.BiometricsScreen>().identifier

    private val _state = MutableStateFlow<BiometricsUiState>(BiometricsUiState.Setup)
    val state = _state.asStateFlow()

    fun onSetup(biometricManager: BiometricManager, password: String) {
        viewModelScope.launch {
            _state.emit(BiometricsUiState.Loading)

            unlockRepository.unlock(identifier, UnsealedVaultCredentials.Password(password))

            unlockRepository.observeVaultStatus(identifier).collectLatest { status ->
                if (status is VaultStatus.Unlocked) {
                    val addUseCase = AddVaultCredentialsUseCase(
                        cryptoContext = biometricManager.cryptoContext,
                        credentialsRepository = credentialsRepository
                    )

                    addUseCase(identifier, UnsealedVaultCredentials.Password(password)).getOrElse {
                        when (it) {
                            AddVaultCredentialsError.Canceled -> _state.emit(BiometricsUiState.Canceled)
                            AddVaultCredentialsError.Unknown -> _state.emit(BiometricsUiState.Failed)
                        }

                        return@collectLatest
                    }

                    _state.emit(BiometricsUiState.Success)
                }

                if (status is VaultStatus.CredentialsFailed) {
                    _state.emit(BiometricsUiState.Failed)
                }
            }
        }
    }

    fun onCancel() {
        viewModelScope.launch {
            unlockRepository.lock(identifier)
        }
    }
}