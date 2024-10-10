package com.maksimowiczm.zebra.core.domain

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.maksimowiczm.zebra.core.data.api.crypto.CryptoContext
import com.maksimowiczm.zebra.core.data.api.crypto.EncryptError
import com.maksimowiczm.zebra.core.data.api.model.SealedVaultCredentials
import com.maksimowiczm.zebra.core.data.api.model.UnsealedVaultCredentials
import com.maksimowiczm.zebra.core.data.api.model.VaultIdentifier
import com.maksimowiczm.zebra.core.data.api.repository.SealedCredentialsRepository

sealed interface AddVaultCredentialsError {
    data object Canceled : AddVaultCredentialsError
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
        val data = when (credentials) {
            is UnsealedVaultCredentials.Password -> {
                cryptoContext.encrypt(credentials.password.toByteArray())
            }
        }.getOrElse {
            when (it) {
                EncryptError.Unknown -> return Err(AddVaultCredentialsError.Unknown)
                EncryptError.Canceled -> return Err(AddVaultCredentialsError.Canceled)
            }
        }

        credentialsRepository.upsertCredentials(
            vaultIdentifier = identifier,
            credentials = SealedVaultCredentials.Password(
                cryptoIdentifier = cryptoContext.getIdentifier(),
                data = data
            )
        )

        return Ok(Unit)
    }
}
