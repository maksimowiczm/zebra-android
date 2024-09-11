package com.maksimowiczm.zebra.core.domain

import com.maksimowiczm.zebra.core.data.model.Vault
import com.maksimowiczm.zebra.core.data.repository.FileRepository
import com.maksimowiczm.zebra.core.data.repository.VaultRepository
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