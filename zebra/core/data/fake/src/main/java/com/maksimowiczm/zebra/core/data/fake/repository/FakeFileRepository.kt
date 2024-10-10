package com.maksimowiczm.zebra.core.data.fake.repository

import android.net.Uri
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.api.repository.FileRepository
import com.maksimowiczm.zebra.core.data.api.repository.OpenFileError
import java.io.InputStream
import javax.inject.Inject

class FakeFileRepository @Inject constructor() : FileRepository {
    override fun persist(uri: Uri): Result<Unit, Unit> {
        return Ok(Unit)
    }

    override fun release(uri: Uri): Result<Unit, Unit> {
        return Ok(Unit)
    }

    override fun isReadable(uri: Uri): Boolean {
        return true
    }

    override fun openInputStream(uri: Uri): Result<InputStream, OpenFileError> {
        return Ok(uri.toString().byteInputStream())
    }
}