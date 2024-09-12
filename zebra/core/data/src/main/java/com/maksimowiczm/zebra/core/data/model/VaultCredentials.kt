package com.maksimowiczm.zebra.core.data.model

sealed interface VaultCredentials {
    data class Password(val password: String) : VaultCredentials
}