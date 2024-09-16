package com.maksimowiczm.zebra.core.domain

import com.maksimowiczm.zebra.core.data.crypto.CryptoContext
import com.maksimowiczm.zebra.core.data.crypto.CryptoResult
import com.maksimowiczm.zebra.core.data.model.SealedVaultCredentials
import com.maksimowiczm.zebra.core.data.model.UnsealedVaultCredentials
import com.maksimowiczm.zebra.core.data.model.VaultIdentifier
import com.maksimowiczm.zebra.core.data.repository.SealedCredentialsRepository

class GetVaultCredentialsUseCase(
    private val cryptoContext: CryptoContext,
    private val credentialsRepository: SealedCredentialsRepository,
) {
    suspend operator fun invoke(identifier: VaultIdentifier): UnsealedVaultCredentials? {
        val credentials = credentialsRepository.getCredentials(identifier) ?: return null

        when (credentials) {
            is SealedVaultCredentials.Password -> {
                val result = cryptoContext.decrypt(credentials.data)
                if (result !is CryptoResult.Success) {
                    return null
                }
                val password = result.data
                return UnsealedVaultCredentials.Password(password.decodeToString())
            }
        }
    }
}