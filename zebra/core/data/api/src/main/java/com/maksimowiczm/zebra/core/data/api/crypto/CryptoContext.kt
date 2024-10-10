package com.maksimowiczm.zebra.core.data.api.crypto

import com.github.michaelbull.result.Result

sealed interface EncryptError {
    data object Canceled : EncryptError
    data object Unknown : EncryptError
}

sealed interface DecryptError {
    data object PermanentlyInvalidated : DecryptError
    data object Canceled : DecryptError
    data object Unknown : DecryptError
}

typealias CryptoIdentifier = ByteArray

interface CryptoContext {
    suspend fun getIdentifier(): CryptoIdentifier
    suspend fun encrypt(data: ByteArray): Result<ByteArray, EncryptError>
    suspend fun decrypt(data: ByteArray): Result<ByteArray, DecryptError>
}