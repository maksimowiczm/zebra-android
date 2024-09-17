package com.maksimowiczm.zebra.core.domain

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.crypto.CryptoContext
import com.maksimowiczm.zebra.core.data.crypto.CryptoResult
import com.maksimowiczm.zebra.core.data.model.SealedVaultCredentials
import com.maksimowiczm.zebra.core.data.model.UnsealedVaultCredentials
import com.maksimowiczm.zebra.core.data.model.VaultIdentifier
import com.maksimowiczm.zebra.core.data.repository.SealedCredentialsRepository

sealed interface GetVaultCredentialsError {
    data object PermanentlyInvalidated : GetVaultCredentialsError
    data object NotFound : GetVaultCredentialsError
    data object Unknown : GetVaultCredentialsError
}

class GetVaultCredentialsUseCase(
    private val cryptoContext: CryptoContext,
    private val credentialsRepository: SealedCredentialsRepository,
) {
    suspend operator fun invoke(identifier: VaultIdentifier): Result<UnsealedVaultCredentials, GetVaultCredentialsError> {
        val credentials = credentialsRepository.getCredentials(identifier)
            ?: return Err(GetVaultCredentialsError.NotFound)

        when (credentials) {
            is SealedVaultCredentials.Password -> {
                return when (val result = cryptoContext.decrypt(credentials.data)) {
                    CryptoResult.Failed -> Err(GetVaultCredentialsError.Unknown)
                    CryptoResult.PermanentlyInvalidated -> {
                        credentialsRepository.deleteAllCredentials()
                        Err(GetVaultCredentialsError.PermanentlyInvalidated)
                    }

                    is CryptoResult.Success -> {
                        val password = result.data
                        Ok(UnsealedVaultCredentials.Password(password.decodeToString()))
                    }
                }
            }
        }
    }
}