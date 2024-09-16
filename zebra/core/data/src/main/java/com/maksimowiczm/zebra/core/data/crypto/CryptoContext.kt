package com.maksimowiczm.zebra.core.data.crypto

sealed interface CryptoResult {
    class Success(val data: ByteArray) : CryptoResult
    data object Failed : CryptoResult
}

interface CryptoContext {
    suspend fun encrypt(data: ByteArray): CryptoResult
    suspend fun decrypt(data: ByteArray): CryptoResult
}