package com.maksimowiczm.zebra.core.data.api.repository

import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.api.model.PeerChannel
import com.maksimowiczm.zebra.core.data.api.model.VaultEntry
import kotlinx.coroutines.flow.Flow

sealed interface CreateError {
    data object PeerChannelAlreadyExists : CreateError
    data object Unknown : CreateError
}

interface PeerChannelRepository {
    fun observePeerChannel(sessionIdentifier: String): Flow<PeerChannel?>
    fun closePeerChannel(sessionIdentifier: String)
    suspend fun connectPeerChannel(sessionIdentifier: String): Result<Unit, CreateError>
    fun sendEntry(sessionIdentifier: String, entry: VaultEntry): Boolean
    fun sendMessage(sessionIdentifier: String, message: String): Boolean
    fun sendMessage(sessionIdentifier: String, message: ByteArray): Boolean
    fun observeMessages(sessionIdentifier: String): Flow<ByteArray>?
}