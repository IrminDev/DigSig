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
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.irmin.digsig.ui.theme.DigSigTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.Signature

class VerifySignatureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DigSigTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VerifySignatureScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun VerifySignatureScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var publicKeyPem by remember { mutableStateOf<String?>(null) }
    var publicKeyLabel by remember { mutableStateOf("") }
    var documentContent by remember { mutableStateOf<String?>(null) }
    var documentName by remember { mutableStateOf("") }
    /** null = not yet verified, true = valid, false = invalid */
    var verificationResult by remember { mutableStateOf<Boolean?>(null) }
    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // Auto-load public key from internal storage if previously generated
    LaunchedEffect(Unit) {
        try {
            val pem = context.openFileInput("key.pub")
                .use { it.readBytes().toString(Charsets.UTF_8) }
            publicKeyPem = pem
            publicKeyLabel = "key.pub (internal storage)"
        } catch (_: Exception) { }
    }

    val pickKeyLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val pem = context.contentResolver.openInputStream(it)
                ?.use { s -> s.readBytes().toString(Charsets.UTF_8) }
            publicKeyPem = pem
            publicKeyLabel = it.lastPathSegment?.substringAfterLast('/') ?: "key.pub"
            verificationResult = null
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
            verificationResult = null
            statusMessage = ""
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
            text = "Verify a Signature",
            style = MaterialTheme.typography.headlineSmall
        )

        // ── Public key card ───────────────────────────────────────────────────
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Public Key", style = MaterialTheme.typography.titleSmall)
                if (publicKeyPem != null) {
                    Text(
                        "✓ $publicKeyLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        "No public key loaded.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = { pickKeyLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Import Public Key…")
                }
            }
        }

        // ── Signed document card ──────────────────────────────────────────────
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Signed Document (.txt)", style = MaterialTheme.typography.titleSmall)
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
                    Text("Select Signed Document…")
                }
            }
        }

        // ── Verify button ─────────────────────────────────────────────────────
        Button(
            onClick = {
                val pubPem = publicKeyPem
                val docContent = documentContent
                if (pubPem == null || docContent == null) {
                    statusMessage = "Load a public key and a document first."
                    isError = true
                    return@Button
                }
                scope.launch(Dispatchers.Default) {
                    try {
                        if (!docContent.contains(SIG_HEADER)) {
                            statusMessage = "No digital signature block found in the document."
                            isError = true
                            verificationResult = false
                            return@launch
                        }
                        val original = docContent.substringBefore(SIG_HEADER)
                        val sigB64 = docContent
                            .substringAfter(SIG_HEADER)
                            .substringBefore(SIG_FOOTER)
                            .trim()
                        val sigBytes = Base64.decode(sigB64, Base64.DEFAULT)

                        val pubKey = parsePemPublicKey(pubPem)
                        val verifier = Signature.getInstance("SHA256withECDSA")
                        verifier.initVerify(pubKey)
                        verifier.update(original.toByteArray(Charsets.UTF_8))
                        val valid = verifier.verify(sigBytes)

                        verificationResult = valid
                        isError = !valid
                        statusMessage = if (valid)
                            "✓ Signature is VALID — the document has not been tampered with."
                        else
                            "✗ Signature is INVALID — the document may have been tampered with."
                    } catch (e: Exception) {
                        statusMessage = "Error verifying: ${e.message}"
                        isError = true
                        verificationResult = false
                    }
                }
            },
            enabled = publicKeyPem != null && documentContent != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify Signature")
        }

        // ── Result banner ─────────────────────────────────────────────────────
        if (statusMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (verificationResult) {
                        true  -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                        false -> MaterialTheme.colorScheme.errorContainer
                        null  -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Text(
                    text = statusMessage,
                    modifier = Modifier.padding(14.dp),
                    color = if (isError) MaterialTheme.colorScheme.error
                            else Color(0xFF2E7D32),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
