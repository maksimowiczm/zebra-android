package com.maksimowiczm.zebra.core.data.api.model

sealed interface UnsealedVaultCredentials {
    data class Password(val password: String) : UnsealedVaultCredentials
}