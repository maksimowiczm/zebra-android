package com.maksimowiczm.zebra.core.data.api.repository

import android.net.Uri
import com.github.michaelbull.result.Result
import java.io.InputStream

interface FileRepository {
    fun persist(uri: Uri): Result<Unit, Unit>
    fun release(uri: Uri): Result<Unit, Unit>
    fun isReadable(uri: Uri): Boolean
    fun openInputStream(uri: Uri): Result<InputStream, OpenFileError>
}

sealed interface OpenFileError {
    data object FileNotFound : OpenFileError
    data object Unknown : OpenFileError
}