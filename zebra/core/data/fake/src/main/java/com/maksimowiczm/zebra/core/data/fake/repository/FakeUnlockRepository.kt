package com.maksimowiczm.zebra.core.data.fake.repository

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.api.model.UnsealedVaultCredentials
import com.maksimowiczm.zebra.core.data.api.model.VaultEntry
import com.maksimowiczm.zebra.core.data.api.model.VaultIdentifier
import com.maksimowiczm.zebra.core.data.api.model.VaultStatus
import com.maksimowiczm.zebra.core.data.api.repository.UnlockError
import com.maksimowiczm.zebra.core.data.api.repository.UnlockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private val entries = listOf(
    VaultEntry(
        identifier = UUID.randomUUID().toString(),
        title = "Entry 1",
        password = { "password" },
        username = "username",
        url = "https://example.com",
    )
)

val status: MutableStateFlow<VaultStatus> = MutableStateFlow(VaultStatus.Locked)

class FakeUnlockRepository @Inject constructor() : UnlockRepository {
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun observeVaultStatus(identifier: VaultIdentifier): Flow<VaultStatus> {
        return status
    }

    override fun getVaultStatus(identifier: VaultIdentifier): VaultStatus {
        return status.value
    }

    override suspend fun unlock(
        identifier: VaultIdentifier,
        credentials: UnsealedVaultCredentials,
    ): Result<Unit, UnlockError> {
        scope.launch {
            status.emit(VaultStatus.Locked)
            delay(100)

            status.emit(VaultStatus.Unlocking)
            delay(2000)

            val fail = when (credentials) {
                is UnsealedVaultCredentials.Password -> credentials.password.isEmpty()
            }
            if (fail) {
                status.emit(VaultStatus.CredentialsFailed(0))
                return@launch
            }

            status.emit(VaultStatus.Unlocked(entries))
        }

        return Ok(Unit)
    }

    override fun lock(identifier: VaultIdentifier) {
        status.value = VaultStatus.Locked
    }
}