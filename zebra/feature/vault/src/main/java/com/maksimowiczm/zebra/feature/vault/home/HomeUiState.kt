package com.maksimowiczm.zebra.feature.vault.home

import com.maksimowiczm.zebra.core.data.api.model.Vault

internal data class HomeUiState(
    val isLoading: Boolean,
    val vaults: List<Vault>
)