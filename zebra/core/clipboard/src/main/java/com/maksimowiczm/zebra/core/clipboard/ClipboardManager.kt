package com.maksimowiczm.zebra.core.clipboard

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Build
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ClipboardManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val clipboard by lazy { context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager }

    fun copy(text: String, confidential: Boolean) {
        val clipData = if (confidential) {
            Log.d(TAG, "Copying confidential data")

            ClipData.newPlainText(LABEL, text).apply {
                description.extras = PersistableBundle().apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                    } else {
                        putBoolean("android.content.extra.IS_SENSITIVE", true)
                    }
                }
            }
        } else {
            Log.d(TAG, "Copying non-confidential data")

            ClipData.newPlainText(LABEL, text)
        }

        clipboard.setPrimaryClip(clipData)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            Toast.makeText(context, context.getString(R.string.copied), Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "ClipboardManager"
        private const val LABEL = "Zebra"
    }
}