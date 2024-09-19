package com.maksimowiczm.zebra.core.data.model

sealed interface VaultStatus {
    data object Locked : VaultStatus
    data object Unlocking : VaultStatus
    data class Unlocked(
        val entries: List<VaultEntry>,
    ) : VaultStatus

    data class CredentialsFailed(val count: Int) : VaultStatus

    data object UnrecoverableError : VaultStatus
}