package com.maksimowiczm.zebra.core.data.fake.repository

import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.api.model.PeerChannel
import com.maksimowiczm.zebra.core.data.api.model.VaultEntry
import com.maksimowiczm.zebra.core.data.api.repository.CreateError
import com.maksimowiczm.zebra.core.data.api.repository.PeerChannelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FakePeerChannelRepository @Inject constructor() : PeerChannelRepository {
    override fun observePeerChannel(sessionIdentifier: String): Flow<PeerChannel?> {
        TODO("Not yet implemented")
    }

    override fun closePeerChannel(sessionIdentifier: String) {
        TODO("Not yet implemented")
    }

    override suspend fun connectPeerChannel(sessionIdentifier: String): Result<Unit, CreateError> {
        TODO("Not yet implemented")
    }

    override fun sendEntry(
        sessionIdentifier: String,
        entry: VaultEntry,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun sendMessage(
        sessionIdentifier: String,
        message: String,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun sendMessage(
        sessionIdentifier: String,
        message: ByteArray,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun observeMessages(sessionIdentifier: String): Flow<ByteArray>? {
        TODO("Not yet implemented")
    }
}