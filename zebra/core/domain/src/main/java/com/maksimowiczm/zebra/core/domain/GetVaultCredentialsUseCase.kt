package com.maksimowiczm.zebra.core.domain

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.maksimowiczm.zebra.core.data.crypto.CryptoContext
import com.maksimowiczm.zebra.core.data.crypto.DecryptError
import com.maksimowiczm.zebra.core.data.model.SealedVaultCredentials
import com.maksimowiczm.zebra.core.data.model.UnsealedVaultCredentials
import com.maksimowiczm.zebra.core.data.model.VaultIdentifier
import com.maksimowiczm.zebra.core.data.repository.SealedCredentialsRepository

sealed interface GetVaultCredentialsError {
    data object PermanentlyInvalidated : GetVaultCredentialsError
    data object NotFound : GetVaultCredentialsError
    data object Canceled : GetVaultCredentialsError
    data class Unknown(val t: Throwable? = null) : GetVaultCredentialsError
}

class GetVaultCredentialsUseCase(
    private val cryptoContext: CryptoContext,
    private val credentialsRepository: SealedCredentialsRepository,
) {
    suspend operator fun invoke(identifier: VaultIdentifier): Result<UnsealedVaultCredentials, GetVaultCredentialsError> {
        val credentials = credentialsRepository.getCredentials(identifier)
            ?: return Err(GetVaultCredentialsError.NotFound)

        if (!credentials.cryptoIdentifier.contentEquals(cryptoContext.getIdentifier())) {
            return Err(GetVaultCredentialsError.PermanentlyInvalidated)
        }

        return when (credentials) {
            is SealedVaultCredentials.Password -> handlePassword(credentials)
        }
    }

    private suspend fun handlePassword(credentials: SealedVaultCredentials.Password): Result<UnsealedVaultCredentials, GetVaultCredentialsError> {
        val data = cryptoContext.decrypt(credentials.data).getOrElse {
            return when (it) {
                DecryptError.PermanentlyInvalidated -> Err(GetVaultCredentialsError.PermanentlyInvalidated)
                DecryptError.Unknown -> Err(GetVaultCredentialsError.Unknown())
                DecryptError.Canceled -> Err(GetVaultCredentialsError.Canceled)
            }
        }

        return Ok(UnsealedVaultCredentials.Password(String(data)))
    }
}