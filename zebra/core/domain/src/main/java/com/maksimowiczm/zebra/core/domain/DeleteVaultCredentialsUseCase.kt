package com.maksimowiczm.zebra.core.domain

import com.maksimowiczm.zebra.core.data.model.VaultIdentifier
import com.maksimowiczm.zebra.core.data.repository.SealedCredentialsRepository
import javax.inject.Inject

class DeleteVaultCredentialsUseCase @Inject constructor(
    private val credentialsRepository: SealedCredentialsRepository,
) {
    suspend operator fun invoke(identifier: VaultIdentifier) {
        credentialsRepository.deleteCredentials(identifier)
    }
}