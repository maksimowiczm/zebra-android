package com.maksimowiczm.zebra.core.domain

import java.net.URI
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.api.model.Vault
import com.maksimowiczm.zebra.core.data.api.repository.FileRepository
import com.maksimowiczm.zebra.core.data.api.repository.VaultRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

sealed interface ImportVaultResult {
    data object FileError : ImportVaultResult
    data class VaultWithPathExists(val vault: Vault) : ImportVaultResult
}

class ImportUniqueVaultUseCase @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val fileRepository: FileRepository,
) {
    suspend operator fun invoke(name: String, path: URI): Result<Unit, ImportVaultResult> {
        try {
            val existingVault = vaultRepository.getVaultByPath(path.toString())
            if (existingVault != null) {
                return Err(ImportVaultResult.VaultWithPathExists(existingVault))
            }

            if (fileRepository.persist(path).isErr) {
                return Err(ImportVaultResult.FileError)
            }

            var vaultName = name

            // Create unique vault name
            while (vaultRepository.vaultExistByName(vaultName)) {
                vaultName = "$vaultName-"
            }

            vaultRepository.upsertVault(
                name = vaultName,
                path = path.toString()
            )

            return Ok(Unit)
        } catch (e: CancellationException) {
            // Do not persist file if import was canceled
            fileRepository.release(path)

            throw e
        }
    }
}