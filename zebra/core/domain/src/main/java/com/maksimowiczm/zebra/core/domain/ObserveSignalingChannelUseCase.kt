package com.maksimowiczm.zebra.core.domain

import com.maksimowiczm.zebra.core.data.repository.PeerChannelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSignalingChannelUseCase @Inject constructor(
    private val peerChannelRepository: PeerChannelRepository,
) {
    operator fun invoke(): Flow<String> {
        return peerChannelRepository.observeSignalingServerUrl()
    }
}