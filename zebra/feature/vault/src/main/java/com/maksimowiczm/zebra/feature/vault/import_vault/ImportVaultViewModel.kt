package com.maksimowiczm.zebra.feature.vault.import_vault

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.getOrElse
import com.maksimowiczm.zebra.core.data.api.repository.VaultRepository
import com.maksimowiczm.zebra.core.domain.ImportUniqueVaultUseCase
import com.maksimowiczm.zebra.core.domain.ImportVaultResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URI
import javax.inject.Inject

@HiltViewModel
internal class ImportVaultViewModel @Inject constructor(
    private val importUniqueVaultUseCase: ImportUniqueVaultUseCase,
    private val vaultRepository: VaultRepository,
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

            val existingVault = vaultRepository.getVaultByPath(uri.toString())

            if (existingVault != null) {
                _state.update { ImportVaultUiState.VaultExists(existingVault) }
                return@launch
            }

            val name = uri.getFileName()
            if (name == null) {
                _state.update {
                    ImportVaultUiState.FileReady(
                        name = "vault",
                        path = uri,
                    )
                }
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
                path = URI.create(state.path.toString())
            ).getOrElse { err ->
                _state.update {
                    when (err) {
                        ImportVaultResult.FileError -> ImportVaultUiState.FileImportError
                        is ImportVaultResult.VaultWithPathExists -> ImportVaultUiState.VaultExists(
                            err.vault
                        )
                    }
                }

                return@launch
            }

            val vault = vaultRepository.getVaultByPath(state.path.toString())

            if (vault == null) {
                _state.update { ImportVaultUiState.FileImportError }
                return@launch
            }

            _state.update { ImportVaultUiState.Done(vault) }
        }
    }
}

private fun Uri.getFileName() = this.path
    ?.substringAfter(":")
    ?.substringBeforeLast(".")
    ?.substringAfterLast("/")