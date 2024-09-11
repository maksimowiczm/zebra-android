package com.maksimowiczm.zebra.feature.vault.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.zebra.core.data.model.Vault
import com.maksimowiczm.zebra.core.data.repository.VaultRepository
import com.maksimowiczm.zebra.core.domain.DeleteVaultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    vaultRepository: VaultRepository,
    private val deleteVaultUseCase: DeleteVaultUseCase,
) : ViewModel() {
    val state = vaultRepository.observeVaults().map { vaults ->
        HomeUiState(
            isLoading = false,
            vaults = vaults,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true, vaults = emptyList())
    )

    fun onDelete(vault: Vault) {
        viewModelScope.launch {
            deleteVaultUseCase(vault)
        }
    }
}