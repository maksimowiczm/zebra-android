package com.maksimowiczm.zebra.core.data.crypto

import com.github.michaelbull.result.Result

sealed interface EncryptError {
    data object Unknown : EncryptError
}

sealed interface DecryptError {
    data object PermanentlyInvalidated : DecryptError
    data object Unknown : DecryptError
}

interface CryptoContext {
    suspend fun getIdentifier(): ByteArray
    suspend fun encrypt(data: ByteArray): Result<ByteArray, EncryptError>
    suspend fun decrypt(data: ByteArray): Result<ByteArray, DecryptError>
}