package com.maksimowiczm.zebra.core.domain

import com.maksimowiczm.zebra.core.data.api.model.Vault
import com.maksimowiczm.zebra.core.data.api.repository.FileRepository
import com.maksimowiczm.zebra.core.data.api.repository.VaultRepository
import javax.inject.Inject

class DeleteVaultUseCase @Inject constructor(
    private val fileRepository: FileRepository,
    private val vaultRepository: VaultRepository,
) {
    suspend operator fun invoke(vault: Vault) {
        vaultRepository.deleteVault(vault)
        fileRepository.release(vault.path)
    }
}