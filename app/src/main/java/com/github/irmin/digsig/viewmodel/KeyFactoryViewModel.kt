package com.github.irmin.digsig.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.irmin.digsig.model.FileStorageModel
import com.github.irmin.digsig.model.generateEcKeyPairPem

class KeyFactoryViewModel {
    var statusMessage by mutableStateOf("")
        private set
    var isError by mutableStateOf(false)
        private set
    var privateKeyPem by mutableStateOf<String?>(null)
        private set
    var publicKeyPem by mutableStateOf<String?>(null)
        private set

    fun generateAndStoreKeyPair(context: Context) {
        try {
            val keys = generateEcKeyPairPem()
            FileStorageModel.saveInternalTextFile(context, "key.priv", keys.privateKeyPem)
            FileStorageModel.saveInternalTextFile(context, "key.pub", keys.publicKeyPem)
            privateKeyPem = keys.privateKeyPem
            publicKeyPem = keys.publicKeyPem
            statusMessage = "Key pair generated and saved to internal storage."
            isError = false
        } catch (e: Exception) {
            statusMessage = "Error: ${e.message}"
            isError = true
        }
    }

    fun onPrivateKeyExported() {
        statusMessage = "Private key exported successfully."
        isError = false
    }

    fun onPublicKeyExported() {
        statusMessage = "Public key exported successfully."
        isError = false
    }
}

