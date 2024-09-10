package com.maksimowiczm.zebra.core.domain

import com.maksimowiczm.zebra.core.data.model.Vault
import com.maksimowiczm.zebra.core.data.repository.VaultRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class InsertUniqueVaultUseCase(
    private val vaultRepository: VaultRepository,
    private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(name: String) {
        withContext(ioDispatcher) {
            var vaultName = name

            // Create unique vault name
            while (vaultRepository.vaultExist(vaultName)) {
                vaultName = "$vaultName-"
            }

            val newVault = Vault(
                name = vaultName
            )

            vaultRepository.insertVault(newVault)
        }
    }
}