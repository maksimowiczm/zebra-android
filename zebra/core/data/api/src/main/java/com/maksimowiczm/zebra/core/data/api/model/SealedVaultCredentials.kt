package com.maksimowiczm.zebra.core.data.api.model

sealed interface SealedVaultCredentials {
    val cryptoIdentifier: ByteArray

    class Password(
        override val cryptoIdentifier: ByteArray,
        val data: ByteArray,
    ) : SealedVaultCredentials
}