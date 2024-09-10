package com.maksimowiczm.zebra.feature.vault.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.zebra.core.data.api.model.Vault
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(
        HomeUiState(
            isLoading = false,
            vaults = emptyList(),
        )
    )
    val state = _state.asStateFlow()

    fun onStart() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val vaults = emptyList<Vault>()
            _state.update { it.copy(isLoading = false, vaults = vaults) }
        }
    }
}