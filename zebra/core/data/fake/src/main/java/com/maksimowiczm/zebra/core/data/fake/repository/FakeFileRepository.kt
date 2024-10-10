package com.maksimowiczm.zebra.core.data.fake.repository

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.api.repository.FileRepository
import com.maksimowiczm.zebra.core.data.api.repository.OpenFileError
import java.io.InputStream
import java.net.URI
import javax.inject.Inject

class FakeFileRepository @Inject constructor() : FileRepository {
    override fun persist(uri: URI): Result<Unit, Unit> {
        return Ok(Unit)
    }

    override fun release(uri: URI): Result<Unit, Unit> {
        return Ok(Unit)
    }

    override fun isReadable(uri: URI): Boolean {
        return true
    }

    override fun openInputStream(uri: URI): Result<InputStream, OpenFileError> {
        return Ok(uri.toString().byteInputStream())
    }
}