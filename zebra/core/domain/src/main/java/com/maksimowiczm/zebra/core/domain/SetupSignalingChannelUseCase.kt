package com.maksimowiczm.zebra.core.domain

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.maksimowiczm.core.zebra.zebra_signal.ZebraSignalClient
import com.maksimowiczm.core.zebra.zebra_signal.ZebraSignalClientFactory
import com.maksimowiczm.zebra.core.data.repository.ZebraSignalRepository
import javax.inject.Inject

sealed interface SetupError {
    data object UnsecureConnection : SetupError
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

            zebraSignalClient.ping().getOrElse {
                return when (it) {
                    is ZebraSignalClient.RequestError.HTTPError,
                    ZebraSignalClient.RequestError.NetworkError,
                    ZebraSignalClient.RequestError.NotValidURL,
                    ZebraSignalClient.RequestError.SerializationError,
                    ZebraSignalClient.RequestError.Unknown,
                    -> Err(SetupError.SignalingChannelPingPongFailed)

                    ZebraSignalClient.RequestError.UnsecureConnection -> Err(SetupError.UnsecureConnection)
                }
            }
        }

        zebraSignalRepository.updateZebraSignalUrl(signalingServer)

        return Ok(Unit)
    }
}