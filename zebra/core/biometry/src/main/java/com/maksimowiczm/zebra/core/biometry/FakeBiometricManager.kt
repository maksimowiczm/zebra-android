package com.maksimowiczm.zebra.core.biometry

import androidx.fragment.app.FragmentActivity
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.api.crypto.CryptoContext
import com.maksimowiczm.zebra.core.data.api.crypto.CryptoIdentifier
import com.maksimowiczm.zebra.core.data.api.crypto.DecryptError
import com.maksimowiczm.zebra.core.data.api.crypto.EncryptError
import com.maksimowiczm.zebra.core.data.api.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull

val fakeIdentifier = byteArrayOf(1, 2, 3)

class FakeBiometricManager(
    fragmentActivity: FragmentActivity,
    mainDispatcher: CoroutineDispatcher,
    defaultDispatcher: CoroutineDispatcher,
    userPreferencesRepository: UserPreferencesRepository,
) : BiometricManagerImpl(
    fragmentActivity,
    mainDispatcher,
    defaultDispatcher,
    userPreferencesRepository
) {
    override fun hasBiometric(): BiometryStatus {
        return BiometryStatus.Ok
    }

    override val cryptoContext: CryptoContext
        get() = FakeBiometricCryptoContext(this)
}

private class FakeBiometricCryptoContext(
    private val biometricManager: BiometricManager,
) : CryptoContext {
    override suspend fun getIdentifier(): CryptoIdentifier {
        return fakeIdentifier
    }

    override suspend fun encrypt(data: ByteArray): Result<ByteArray, EncryptError> {
        biometricManager.authenticate().firstOrNull { it is AuthenticationResult.Success }
            ?: return Err(EncryptError.Canceled)

        return Ok(data)
    }

    override suspend fun decrypt(data: ByteArray): Result<ByteArray, DecryptError> {
        biometricManager.authenticate().firstOrNull { it is AuthenticationResult.Success }
            ?: return Err(DecryptError.Canceled)

        return Ok(data)
    }
}