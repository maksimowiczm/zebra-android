package com.maksimowiczm.zebra.core.data.fake.repository

import android.net.Uri
import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.api.repository.FileRepository
import com.maksimowiczm.zebra.core.data.api.repository.OpenFileError
import java.io.InputStream
import javax.inject.Inject

class FakeFileRepository @Inject constructor() : FileRepository {
    override fun persist(uri: Uri): Result<Unit, Unit> {
        TODO("Not yet implemented")
    }

    override fun release(uri: Uri): Result<Unit, Unit> {
        TODO("Not yet implemented")
    }

    override fun isReadable(uri: Uri): Boolean {
        TODO("Not yet implemented")
    }

    override fun openInputStream(uri: Uri): Result<InputStream, OpenFileError> {
        TODO("Not yet implemented")
    }
}