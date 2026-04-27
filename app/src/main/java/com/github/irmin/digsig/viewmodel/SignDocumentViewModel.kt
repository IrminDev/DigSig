package com.github.irmin.digsig.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.irmin.digsig.model.FileStorageModel
import com.github.irmin.digsig.model.signTextDocument

class SignDocumentViewModel {
    var privateKeyPem by mutableStateOf<String?>(null)
        private set
    var privateKeyLabel by mutableStateOf("")
        private set
    var documentContent by mutableStateOf<String?>(null)
        private set
    var documentName by mutableStateOf("")
        private set
    var signedContent by mutableStateOf<String?>(null)
        private set
    var statusMessage by mutableStateOf("")
        private set
    var isError by mutableStateOf(false)
        private set

    fun loadInternalPrivateKey(context: Context) {
        val pem = FileStorageModel.readInternalTextFileOrNull(context, "key.priv") ?: return
        privateKeyPem = pem
        privateKeyLabel = "key.priv (internal storage)"
    }

    fun importPrivateKey(context: Context, uri: Uri) {
        privateKeyPem = FileStorageModel.readTextFromUriOrNull(context, uri)
        privateKeyLabel = uri.lastPathSegment?.substringAfterLast('/') ?: "key.priv"
    }

    fun importDocument(context: Context, uri: Uri) {
        documentContent = FileStorageModel.readTextFromUriOrNull(context, uri)
        documentName = uri.lastPathSegment?.substringAfterLast('/') ?: "document.txt"
        signedContent = null
    }

    fun signDocument() {
        val privPem = privateKeyPem
        val docContent = documentContent
        if (privPem == null || docContent == null) {
            statusMessage = "Load a private key and a document first."
            isError = true
            return
        }
        try {
            signedContent = signTextDocument(privPem, docContent)
            statusMessage = "Document signed. Tap 'Save Signed Document' to export."
            isError = false
        } catch (e: Exception) {
            statusMessage = "Error signing: ${e.message}"
            isError = true
        }
    }

    fun saveSignedDocument(context: Context, uri: Uri) {
        val content = signedContent ?: return
        FileStorageModel.writeTextToUri(context, uri, content)
        statusMessage = "Signed document saved."
        isError = false
    }
}

