package com.maksimowiczm.zebra.feature.vault.biometrics

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.maksimowiczm.zebra.core.biometry.BiometricManager
import com.maksimowiczm.zebra.core.data.model.UnsealedVaultCredentials
import com.maksimowiczm.zebra.core.data.model.VaultStatus
import com.maksimowiczm.zebra.core.data.repository.SealedCredentialsRepository
import com.maksimowiczm.zebra.core.data.repository.UnlockRepository
import com.maksimowiczm.zebra.core.domain.AddVaultCredentialsUseCase
import com.maksimowiczm.zebra.feature.vault.VaultScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BiometricsViewModel @Inject constructor(
    private val credentialsRepository: SealedCredentialsRepository,
    private val unlockRepository: UnlockRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val identifier = savedStateHandle.toRoute<VaultScreen.BiometricsScreen>().identifier

    private val _state = MutableStateFlow<BiometricsUiState>(BiometricsUiState.Setup)
    val state = _state.asStateFlow()

    fun onSetup(biometricManager: BiometricManager, password: String) {
        viewModelScope.launch {
            _state.update { BiometricsUiState.Loading }

            unlockRepository.unlock(identifier, UnsealedVaultCredentials.Password(password))

            unlockRepository.observeVaultStatus(identifier).collectLatest { status ->
                Log.d(TAG, "onSetup: $status")

                if (status is VaultStatus.Unlocked) {
                    val addUseCase = AddVaultCredentialsUseCase(
                        cryptoContext = biometricManager.cryptoContext,
                        credentialsRepository = credentialsRepository
                    )

                    addUseCase(identifier, UnsealedVaultCredentials.Password(password))
                    _state.update { BiometricsUiState.Success }
                }

                if (status is VaultStatus.Failed) {
                    _state.update { BiometricsUiState.Failed }
                }
            }
        }
    }

    companion object {
        private const val TAG = "BiometricsViewModel"
    }
}