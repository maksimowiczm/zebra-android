package com.maksimowiczm.zebra.feature.vault.import_vault

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.getOrElse
import com.maksimowiczm.zebra.core.domain.ImportUniqueVaultUseCase
import com.maksimowiczm.zebra.core.domain.ImportVaultResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ImportVaultViewModel @Inject constructor(
    private val importUniqueVaultUseCase: ImportUniqueVaultUseCase
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
                _state.update { ImportVaultUiState.IllegalFileName }
                return@launch
            }

            _state.update {
                ImportVaultUiState.FileReady(
                    name = name,
                    path = uri,
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

        viewModelScope.launch {
            _state.update { ImportVaultUiState.Loading }

            importUniqueVaultUseCase.invoke(
                name = state.name,
                path = state.path
            ).getOrElse { err ->
                val newState = when (err) {
                    ImportVaultResult.FileError -> ImportVaultUiState.FileImportError
                    is ImportVaultResult.VaultExists -> ImportVaultUiState.VaultExists(err.vault)
                }

                _state.update { newState }

                return@launch
            }

            _state.update { ImportVaultUiState.Done }
        }
    }
}

private fun Uri.getFileName() = this.path
    ?.substringAfter(":")
    ?.substringBeforeLast(".")
    ?.substringAfterLast("/")