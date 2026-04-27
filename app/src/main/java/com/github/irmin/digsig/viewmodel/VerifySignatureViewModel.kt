package com.github.irmin.digsig.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.irmin.digsig.model.FileStorageModel
import com.github.irmin.digsig.model.SIG_FOOTER
import com.github.irmin.digsig.model.SIG_HEADER
import com.github.irmin.digsig.model.verifySignedTextDocument

class VerifySignatureViewModel {
    var publicKeyPem by mutableStateOf<String?>(null)
        private set
    var publicKeyLabel by mutableStateOf("")
        private set
    var documentContent by mutableStateOf<String?>(null)
        private set
    var documentName by mutableStateOf("")
        private set
    var verificationResult by mutableStateOf<Boolean?>(null)
        private set
    var statusMessage by mutableStateOf("")
        private set
    var isError by mutableStateOf(false)
        private set

    fun loadInternalPublicKey(context: Context) {
        val pem = FileStorageModel.readInternalTextFileOrNull(context, "key.pub") ?: return
        publicKeyPem = pem
        publicKeyLabel = "key.pub (internal storage)"
    }

    fun importPublicKey(context: Context, uri: Uri) {
        publicKeyPem = FileStorageModel.readTextFromUriOrNull(context, uri)
        publicKeyLabel = uri.lastPathSegment?.substringAfterLast('/') ?: "key.pub"
        verificationResult = null
    }

    fun importDocument(context: Context, uri: Uri) {
        documentContent = FileStorageModel.readTextFromUriOrNull(context, uri)
        documentName = uri.lastPathSegment?.substringAfterLast('/') ?: "document.txt"
        verificationResult = null
        statusMessage = ""
    }

    fun verify() {
        val pubPem = publicKeyPem
        val docContent = documentContent
        if (pubPem == null || docContent == null) {
            statusMessage = "Load a public key and a document first."
            isError = true
            return
        }
        try {
            if (!docContent.contains(SIG_HEADER) || !docContent.contains(SIG_FOOTER)) {
                statusMessage = "No digital signature block found in the document."
                isError = true
                verificationResult = false
                return
            }
            val valid = verifySignedTextDocument(pubPem, docContent)
            verificationResult = valid
            isError = !valid
            statusMessage = if (valid) {
                "✓ Signature is VALID — the document has not been tampered with."
            } else {
                "✗ Signature is INVALID — the document may have been tampered with."
            }
        } catch (e: Exception) {
            statusMessage = "Error verifying: ${e.message}"
            isError = true
            verificationResult = false
        }
    }
}

