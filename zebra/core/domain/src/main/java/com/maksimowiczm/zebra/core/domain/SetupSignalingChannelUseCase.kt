package com.maksimowiczm.zebra.core.domain

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.maksimowiczm.core.zebra.zebra_signal.ZebraSignalClientFactory
import com.maksimowiczm.zebra.core.data.repository.ZebraSignalRepository
import javax.inject.Inject

sealed interface SetupError {
    data object SignalingChannelPingPongFailed : SetupError
}

class SetupSignalingChannelUseCase @Inject constructor(
    private val zebraSignalRepository: ZebraSignalRepository,
    private val zebraSignalClientFactory: ZebraSignalClientFactory,
) {
    suspend operator fun invoke(
        signalingServer: String,
        validate: Boolean = true,
    ): Result<Unit, SetupError> {
        if (validate) {
            val zebraSignalClient = zebraSignalClientFactory.create(signalingServer)
            val pingResult = zebraSignalClient.ping()

            if (!pingResult) {
                return Err(SetupError.SignalingChannelPingPongFailed)
            }
        }

        zebraSignalRepository.updateZebraSignalUrl(signalingServer)

        return Ok(Unit)
    }
}