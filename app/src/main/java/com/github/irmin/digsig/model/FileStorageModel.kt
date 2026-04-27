package com.github.irmin.digsig.model

import android.content.Context
import android.net.Uri

object FileStorageModel {
    fun saveInternalTextFile(context: Context, fileName: String, content: String) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { it.write(content.toByteArray()) }
    }

    fun readInternalTextFileOrNull(context: Context, fileName: String): String? =
        try {
            context.openFileInput(fileName).use { it.readBytes().toString(Charsets.UTF_8) }
        } catch (_: Exception) {
            null
        }

    fun readTextFromUriOrNull(context: Context, uri: Uri): String? =
        context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }

    fun writeTextToUri(context: Context, uri: Uri, content: String) {
        context.contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) }
    }
}

