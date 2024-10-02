package com.maksimowiczm.zebra.feature.share.connection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.maksimowiczm.zebra.feature.share.ShareScreen
import com.maksimowiczm.zebra.core.common.combineN
import com.maksimowiczm.zebra.core.data.model.VaultEntryIdentifier
import com.maksimowiczm.zebra.core.data.model.VaultIdentifier
import com.maksimowiczm.zebra.core.data.model.VaultStatus
import com.maksimowiczm.zebra.core.data.repository.PeerChannelRepository
import com.maksimowiczm.zebra.core.data.repository.UnlockRepository
import com.maksimowiczm.zebra.core.domain.ConnectPeerChannelWithTimeoutUseCase
import com.maksimowiczm.zebra.core.domain.ConnectPeerChannelWithTimeoutUseCase.PeerChannelStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
internal class ConnectionViewModel @Inject constructor(
    private val peerChannelRepository: PeerChannelRepository,
    connectPeerChannelWithTimeoutUseCase: ConnectPeerChannelWithTimeoutUseCase,
    unlockRepository: UnlockRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val vaultIdentifier: VaultIdentifier
    private val entryIdentifier: VaultEntryIdentifier
    private val session: String

    init {
        val route = savedStateHandle.toRoute<ShareScreen.ConnectionScreen>()
        vaultIdentifier = route.vaultIdentifier
        entryIdentifier = route.entryIdentifier
        session = route.session
    }

    private var success: Boolean = false

    val state: StateFlow<ConnectionUiState> = combineN(
        unlockRepository.observeVaultStatus(vaultIdentifier),
        connectPeerChannelWithTimeoutUseCase(session = session, timeout = 5_000)
    ) { vaultStatus, peerChannelStatus ->
        when (vaultStatus) {
            is VaultStatus.Unlocked -> {}

            is VaultStatus.CredentialsFailed,
            VaultStatus.Locked,
            VaultStatus.Unlocking,
            -> return@combineN ConnectionUiState.VaultLocked

            VaultStatus.UnrecoverableError -> return@combineN ConnectionUiState.Failed
        }

        return@combineN when (peerChannelStatus) {
            PeerChannelStatus.Connecting -> ConnectionUiState.Loading
            PeerChannelStatus.Connected -> onConnected(vaultStatus)
            PeerChannelStatus.Closed -> {
                if (success) {
                    ConnectionUiState.Done
                } else {
                    ConnectionUiState.Failed
                }
            }

            PeerChannelStatus.ClosedWithTimeout -> ConnectionUiState.Timeout
            is PeerChannelStatus.CreateError,
            PeerChannelStatus.Failed,
            -> ConnectionUiState.Failed
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ConnectionUiState.Loading
    )

    private fun onConnected(vaultStatus: VaultStatus.Unlocked): ConnectionUiState {
        val entry = vaultStatus.entries.find { it.identifier == entryIdentifier }

        if (entry == null) {
            peerChannelRepository.closePeerChannel(session)
            return ConnectionUiState.Failed
        }

        if (!peerChannelRepository.sendEntry(session, entry)) {
            peerChannelRepository.closePeerChannel(session)
            return ConnectionUiState.Failed
        }

        success = true
        peerChannelRepository.closePeerChannel(session)

        return ConnectionUiState.Done
    }

    fun onCancel() {
        peerChannelRepository.closePeerChannel(session)
    }
}