package com.maksimowiczm.zebra.core.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.maksimowiczm.zebra.core.data.api.repository.FileRepository
import com.maksimowiczm.zebra.core.data.api.repository.OpenFileError
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileNotFoundException
import java.io.InputStream
import javax.inject.Inject


class FileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : FileRepository {
    override fun persist(uri: Uri): Result<Unit, Unit> {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            return Ok(Unit)
        } catch (_: Exception) {
            return Err(Unit)
        }
    }

    override fun release(uri: Uri): Result<Unit, Unit> {
        try {
            context.contentResolver.releasePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            return Ok(Unit)
        } catch (_: Exception) {
            return Err(Unit)
        }
    }

    override fun isReadable(uri: Uri): Boolean {
        // todo this might be really bad idea to do
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { }
            return true
        } catch (_: Exception) {
            return false
        }
    }

    override fun openInputStream(uri: Uri): Result<InputStream, OpenFileError> {
        val stream = runCatching {
            context.contentResolver.openInputStream(uri)
        }.getOrElse {
            if (it is FileNotFoundException) {
                return Err(OpenFileError.FileNotFound)
            }

            return Err(OpenFileError.Unknown)
        }

        if (stream == null) {
            return Err(OpenFileError.Unknown)
        }

        return Ok(stream)
    }
}