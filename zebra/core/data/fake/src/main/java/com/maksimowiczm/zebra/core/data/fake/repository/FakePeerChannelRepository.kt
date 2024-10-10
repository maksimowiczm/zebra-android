package com.maksimowiczm.zebra.core.data.fake.repository

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.api.model.PeerChannel
import com.maksimowiczm.zebra.core.data.api.model.VaultEntry
import com.maksimowiczm.zebra.core.data.api.repository.CreateError
import com.maksimowiczm.zebra.core.data.api.repository.PeerChannelRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

val peerStatus: MutableStateFlow<PeerChannel> = MutableStateFlow(PeerChannel.Connecting)

class FakePeerChannelRepository @Inject constructor() : PeerChannelRepository {
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun observePeerChannel(sessionIdentifier: String): Flow<PeerChannel?> {
        return peerStatus
    }

    override fun closePeerChannel(sessionIdentifier: String) {
        peerStatus.value = PeerChannel.Closed
    }

    override suspend fun connectPeerChannel(sessionIdentifier: String): Result<Unit, CreateError> {
        scope.launch {
            delay(1000)
            peerStatus.emit(PeerChannel.Connected)
        }

        return Ok(Unit)
    }

    override fun sendEntry(
        sessionIdentifier: String,
        entry: VaultEntry,
    ): Boolean {
        return true
    }

    override fun sendMessage(
        sessionIdentifier: String,
        message: String,
    ): Boolean {
        return true
    }

    override fun sendMessage(
        sessionIdentifier: String,
        message: ByteArray,
    ): Boolean {
        return true
    }

    override fun observeMessages(sessionIdentifier: String): Flow<ByteArray>? {
        return null
    }
}