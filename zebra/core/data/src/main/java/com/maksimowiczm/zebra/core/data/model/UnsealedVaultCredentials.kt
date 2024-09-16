package com.maksimowiczm.zebra.core.data.model

sealed interface UnsealedVaultCredentials {
    data class Password(val password: String) : UnsealedVaultCredentials
}