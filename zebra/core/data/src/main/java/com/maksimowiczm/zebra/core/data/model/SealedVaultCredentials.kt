package com.maksimowiczm.zebra.core.data.model

sealed interface SealedVaultCredentials {
    class Password(val data: ByteArray) : SealedVaultCredentials
}