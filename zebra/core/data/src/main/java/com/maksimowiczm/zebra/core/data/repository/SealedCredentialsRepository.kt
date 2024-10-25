package com.maksimowiczm.zebra.core.data.repository

import com.maksimowiczm.zebra.core.data.model.SealedVaultCredentials
import com.maksimowiczm.zebra.core.data.model.VaultIdentifier
import com.maksimowiczm.zebra.core.database.dao.CredentialsDao
import com.maksimowiczm.zebra.core.database.model.CredentialsEntity
import com.maksimowiczm.zebra.core.database.model.CredentialsType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SealedCredentialsRepository @Inject constructor(
    private val credentialsDao: CredentialsDao,
) {
    fun observeCredentialsAvailable(vaultIdentifier: VaultIdentifier): Flow<Boolean> {
        return credentialsDao.observeHasCredentialsByIdentifier(vaultIdentifier)
    }

    suspend fun getCredentials(vaultIdentifier: VaultIdentifier): SealedVaultCredentials? {
        return credentialsDao.getCredentials(vaultIdentifier)?.toEncryptedVaultCredentials()
    }

    suspend fun upsertCredentials(
        vaultIdentifier: VaultIdentifier,
        credentials: SealedVaultCredentials,
    ) {
        val entity = when (credentials) {
            is SealedVaultCredentials.Password -> CredentialsEntity(
                vaultIdentifier = vaultIdentifier,
                data = credentials.data,
                type = CredentialsType.Password,
                cryptoIdentifier = credentials.cryptoIdentifier,
            )
        }

        credentialsDao.upsertCredentials(entity)
    }

    suspend fun deleteCredentials(vaultIdentifier: VaultIdentifier) {
        credentialsDao.deleteCredentials(vaultIdentifier)
    }
}

private fun CredentialsEntity.toEncryptedVaultCredentials(): SealedVaultCredentials {
    return SealedVaultCredentials.Password(
        cryptoIdentifier = cryptoIdentifier,
        data = data,
    )
}