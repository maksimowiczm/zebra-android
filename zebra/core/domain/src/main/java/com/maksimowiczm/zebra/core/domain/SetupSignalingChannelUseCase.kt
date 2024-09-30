package com.maksimowiczm.zebra.core.domain

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.repository.ZebraSignalRepository
import javax.inject.Inject

sealed interface SetupError {
    data object SignalingChannelPingPongFailed : SetupError
}

class SetupSignalingChannelUseCase @Inject constructor(
    private val zebraSignalRepository: ZebraSignalRepository,
) {
    suspend operator fun invoke(signalingServer: String): Result<Unit, SetupError> {
        zebraSignalRepository.updateZebraSignalUrl(signalingServer)

        return Ok(Unit)
    }
}