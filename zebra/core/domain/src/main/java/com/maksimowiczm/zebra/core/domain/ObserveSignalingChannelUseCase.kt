package com.maksimowiczm.zebra.core.domain

import com.maksimowiczm.zebra.core.data.api.repository.ZebraSignalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSignalingChannelUseCase @Inject constructor(
    private val zebraSignalRepository: ZebraSignalRepository
) {
    operator fun invoke(): Flow<String> {
        return zebraSignalRepository.observeZebraSignalUrl()
    }
}