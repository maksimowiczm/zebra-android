package com.maksimowiczm.zebra.feature.vault.import_vault

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.zebra.core.domain.InsertUniqueVaultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportVaultViewModel @Inject constructor(
    private val insertUniqueVaultUseCase: InsertUniqueVaultUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<ImportVaultUiState>(ImportVaultUiState.PickFile)
    val state = _state.asStateFlow()

    fun onRetry() {
        _state.update { ImportVaultUiState.PickFile }
    }

    fun onCanceled() {
        _state.update { ImportVaultUiState.PickFileCanceled }
    }

    fun onFilePicked(uri: Uri) {
        viewModelScope.launch {
            _state.update { ImportVaultUiState.Loading }

            val name = uri.getFileName()
            if (name == null) {
                _state.update { ImportVaultUiState.FileError }
                return@launch
            }

            _state.update {
                ImportVaultUiState.FileReady(
                    name = name,
                )
            }
        }
    }

    fun onNameChanged(name: String) {
        val state = _state.value
        if (state is ImportVaultUiState.FileReady) {
            _state.update { state.copy(name = name) }
        }
    }

    fun onImport() {
        if (state.value !is ImportVaultUiState.FileReady) {
            return
        }

        val state = (state.value as ImportVaultUiState.FileReady)
        val name = state.name

        viewModelScope.launch {
            _state.update { ImportVaultUiState.Loading }

            insertUniqueVaultUseCase.invoke(name)

            _state.update { ImportVaultUiState.Done }
        }
    }
}

private fun Uri.getFileName() = this.path
    ?.substringAfter(":")
    ?.substringBeforeLast(".")
    ?.substringAfterLast("/")