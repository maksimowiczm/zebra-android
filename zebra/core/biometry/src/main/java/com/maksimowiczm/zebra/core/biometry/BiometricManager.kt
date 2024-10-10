package com.maksimowiczm.zebra.core.biometry

import kotlinx.coroutines.flow.Flow
import javax.crypto.Cipher

sealed interface BiometryStatus {
    data object Ok : BiometryStatus
    data object NotAvailable : BiometryStatus
    data object NotEnrolled : BiometryStatus
    data object NotSupported : BiometryStatus
}

sealed interface AuthenticationResult {
    data class Success(val cipher: Cipher?) : AuthenticationResult
    data object Failed : AuthenticationResult
    data object Error : AuthenticationResult
}

interface BiometricManager {
    fun hasBiometric(): BiometryStatus
    fun authenticate(cipher: Cipher? = null): Flow<AuthenticationResult>
    val cryptoContext: BiometricCryptoContext
}