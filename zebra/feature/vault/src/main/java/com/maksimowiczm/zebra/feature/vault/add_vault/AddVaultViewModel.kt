package com.maksimowiczm.zebra.feature.vault.add_vault

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// fake impl
class AddVaultViewModel : ViewModel() {
    private val _state = MutableStateFlow<AddVaultUiState>(AddVaultUiState.Idle)
    val state = _state.asStateFlow()

    fun onRetry() {
        _state.update { AddVaultUiState.PickFile }
    }

    fun onStart() {
        if (_state.value is AddVaultUiState.Idle) {
            _state.update { AddVaultUiState.PickFile }
        }
    }

    fun onCanceled() {
        _state.update { AddVaultUiState.PickFileCanceled }
    }

    fun onFilePicked(uri: Uri) {
        viewModelScope.launch {
            _state.update { AddVaultUiState.Loading }
            delay(100)
            _state.update {
                AddVaultUiState.FileReady(
                    name = uri.host ?: "file name"
                )
            }
        }
    }

    fun onNameChanged(name: String) {
        val state = _state.value
        if (state is AddVaultUiState.FileReady) {
            _state.update { state.copy(name = name) }
        }
    }

    fun onAdd() {
        viewModelScope.launch {
            _state.update { AddVaultUiState.Loading }
            delay(50)
            _state.update { AddVaultUiState.Done }
        }
    }
}