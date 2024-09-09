package com.maksimowiczm.zebra.feature_vault.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(
        HomeUiState(
            isLoading = false,
            vaults = emptyList(),
        )
    )
    val state = _state.asStateFlow()
}