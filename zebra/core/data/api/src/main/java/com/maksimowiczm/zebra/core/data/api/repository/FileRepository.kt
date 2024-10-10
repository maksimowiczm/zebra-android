package com.maksimowiczm.zebra.core.data.api.repository

import com.github.michaelbull.result.Result
import java.io.InputStream
import java.net.URI

interface FileRepository {
    fun persist(uri: URI): Result<Unit, Unit>
    fun release(uri: URI): Result<Unit, Unit>
    fun isReadable(uri: URI): Boolean
    fun openInputStream(uri: URI): Result<InputStream, OpenFileError>
}

sealed interface OpenFileError {
    data object FileNotFound : OpenFileError
    data object Unknown : OpenFileError
}