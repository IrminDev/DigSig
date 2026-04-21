package com.github.irmin.digsig

import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.irmin.digsig.ui.theme.DigSigTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.Signature

class SignDocumentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DigSigTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SignDocumentScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun SignDocumentScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var privateKeyPem by remember { mutableStateOf<String?>(null) }
    var privateKeyLabel by remember { mutableStateOf("") }
    var documentContent by remember { mutableStateOf<String?>(null) }
    var documentName by remember { mutableStateOf("") }
    var signedContent by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // Auto-load private key from internal storage if previously generated
    LaunchedEffect(Unit) {
        try {
            val pem = context.openFileInput("key.priv")
                .use { it.readBytes().toString(Charsets.UTF_8) }
            privateKeyPem = pem
            privateKeyLabel = "key.priv (internal storage)"
        } catch (_: Exception) { }
    }

    val pickKeyLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val pem = context.contentResolver.openInputStream(it)
                ?.use { s -> s.readBytes().toString(Charsets.UTF_8) }
            privateKeyPem = pem
            privateKeyLabel = it.lastPathSegment?.substringAfterLast('/') ?: "key.priv"
        }
    }

    val pickDocLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val content = context.contentResolver.openInputStream(it)
                ?.use { s -> s.readBytes().toString(Charsets.UTF_8) }
            documentContent = content
            documentName = it.lastPathSegment?.substringAfterLast('/') ?: "document.txt"
            signedContent = null
        }
    }

    val saveDocLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        uri?.let {
            val content = signedContent ?: return@let
            context.contentResolver.openOutputStream(it)
                ?.use { os -> os.write(content.toByteArray()) }
            statusMessage = "✓ Signed document saved."
            isError = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Sign a Document",
            style = MaterialTheme.typography.headlineSmall
        )

        // ── Private key card ─────────────────────────────────────────────────
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Private Key", style = MaterialTheme.typography.titleSmall)
                if (privateKeyPem != null) {
                    Text(
                        "✓ $privateKeyLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        "No private key loaded.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = { pickKeyLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Import Private Key…")
                }
            }
        }

        // ── Document card ─────────────────────────────────────────────────────
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Document (.txt)", style = MaterialTheme.typography.titleSmall)
                if (documentContent != null) {
                    Text(
                        "✓ $documentName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        "No document selected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = { pickDocLauncher.launch(arrayOf("text/plain", "*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select .txt Document…")
                }
            }
        }

        // ── Actions ───────────────────────────────────────────────────────────
        Button(
            onClick = {
                val privPem = privateKeyPem
                val docContent = documentContent
                if (privPem == null || docContent == null) {
                    statusMessage = "Load a private key and a document first."
                    isError = true
                    return@Button
                }
                scope.launch(Dispatchers.Default) {
                    try {
                        // Strip any pre-existing signature so we sign the raw text
                        val original = stripSignature(docContent)
                        val privKey = parsePemPrivateKey(privPem)
                        val signer = Signature.getInstance("SHA256withECDSA")
                        signer.initSign(privKey)
                        signer.update(original.toByteArray(Charsets.UTF_8))
                        val sigB64 = Base64.encodeToString(signer.sign(), Base64.NO_WRAP)
                        signedContent = original + SIG_HEADER + sigB64 + SIG_FOOTER
                        statusMessage = "✓ Document signed. Tap 'Save Signed Document' to export."
                        isError = false
                    } catch (e: Exception) {
                        statusMessage = "Error signing: ${e.message}"
                        isError = true
                    }
                }
            },
            enabled = privateKeyPem != null && documentContent != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Document")
        }

        if (signedContent != null) {
            Button(
                onClick = {
                    val suggested = "signed_${documentName.substringAfterLast('/')}"
                    saveDocLauncher.launch(suggested)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Signed Document…")
            }
        }

        if (statusMessage.isNotEmpty()) {
            Text(
                text = statusMessage,
                color = if (isError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
