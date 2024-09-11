package com.maksimowiczm.zebra.core.domain

import android.net.Uri
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.maksimowiczm.zebra.core.data.repository.FileRepository
import com.maksimowiczm.zebra.core.data.repository.VaultRepository
import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.model.Vault
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

sealed interface ImportVaultResult {
    data object FileError : ImportVaultResult
    data class VaultExists(val vault: Vault) : ImportVaultResult
}

class ImportUniqueVaultUseCase @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val fileRepository: FileRepository,
) {
    suspend operator fun invoke(name: String, path: Uri): Result<Unit, ImportVaultResult> {
        try {
            val existingVault = vaultRepository.getVaultByPath(path.toString())
            if (existingVault != null) {
                return Err(ImportVaultResult.VaultExists(existingVault))
            }

            if (fileRepository.persist(path).isErr) {
                return Err(ImportVaultResult.FileError)
            }

            var vaultName = name

            // Create unique vault name
            while (vaultRepository.vaultExistByName(vaultName)) {
                vaultName = "$vaultName-"
            }

            val newVault = Vault(
                name = vaultName,
                path = path,
            )

            vaultRepository.upsertVault(newVault)

            return Ok(Unit)
        } catch (e: CancellationException) {
            // Do not persist file if import was canceled
            fileRepository.release(path)

            throw e
        }
    }
}