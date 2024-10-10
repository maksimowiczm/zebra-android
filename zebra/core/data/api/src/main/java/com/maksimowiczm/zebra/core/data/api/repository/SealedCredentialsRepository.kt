package com.maksimowiczm.zebra.core.data.api.repository

import com.maksimowiczm.zebra.core.data.api.model.SealedVaultCredentials
import com.maksimowiczm.zebra.core.data.api.model.VaultIdentifier
import kotlinx.coroutines.flow.Flow


interface SealedCredentialsRepository {
    fun observeCredentialsAvailable(vaultIdentifier: VaultIdentifier): Flow<Boolean>

    suspend fun getCredentials(vaultIdentifier: VaultIdentifier): SealedVaultCredentials?

    suspend fun upsertCredentials(
        vaultIdentifier: VaultIdentifier,
        credentials: SealedVaultCredentials,
    )

    suspend fun deleteCredentials(vaultIdentifier: VaultIdentifier)
}