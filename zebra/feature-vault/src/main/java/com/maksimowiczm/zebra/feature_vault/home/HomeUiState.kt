package com.maksimowiczm.zebra.feature_vault.home

import com.maksimowiczm.zebra.domain.api.model.Vault

internal data class HomeUiState(
    val isLoading: Boolean,
    val vaults: List<Vault>
)