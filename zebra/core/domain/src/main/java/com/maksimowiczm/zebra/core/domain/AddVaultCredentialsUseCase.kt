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

sealed interface AddVaultCredentialsError {
    data object Unknown : AddVaultCredentialsError
}

class AddVaultCredentialsUseCase(
    private val cryptoContext: CryptoContext,
    private val credentialsRepository: SealedCredentialsRepository,
) {
    suspend operator fun invoke(
        identifier: VaultIdentifier,
        credentials: UnsealedVaultCredentials,
    ): Result<Unit, AddVaultCredentialsError> {
        val result = when (credentials) {
            is UnsealedVaultCredentials.Password -> {
                cryptoContext.encrypt(credentials.password.toByteArray())
            }
        }

        val data = when (result) {
            CryptoResult.Failed -> return Err(AddVaultCredentialsError.Unknown)
            is CryptoResult.Success -> result.data
        }

        credentialsRepository.upsertCredentials(
            vaultIdentifier = identifier,
            credentials = SealedVaultCredentials.Password(data)
        )

        return Ok(Unit)
    }
}
