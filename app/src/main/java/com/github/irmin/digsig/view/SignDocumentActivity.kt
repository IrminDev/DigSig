package com.github.irmin.digsig.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.irmin.digsig.ui.components.AppScreen
import com.github.irmin.digsig.ui.components.HeroHeader
import com.github.irmin.digsig.ui.components.InfoPill
import com.github.irmin.digsig.ui.components.SectionCard
import com.github.irmin.digsig.ui.components.StatusBanner
import com.github.irmin.digsig.ui.theme.Blue80
import com.github.irmin.digsig.ui.theme.DigSigTheme
import com.github.irmin.digsig.ui.theme.Indigo80
import com.github.irmin.digsig.ui.theme.Teal80
import com.github.irmin.digsig.viewmodel.SignDocumentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignDocumentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DigSigTheme {
                Scaffold { innerPadding ->
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
    val viewModel = remember { SignDocumentViewModel() }

    LaunchedEffect(Unit) {
        viewModel.loadInternalPrivateKey(context)
    }

    val pickKeyLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importPrivateKey(context, it) }
    }

    val pickDocLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importDocument(context, it) }
    }

    val saveDocLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        uri?.let { viewModel.saveSignedDocument(context, it) }
    }

    AppScreen(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HeroHeader(
                title = "Sign a Document",
                subtitle = "Load your private key, choose a text file, and append a signature block with a polished guided flow.",
                icon = Icons.Rounded.Edit,
                badges = listOf("Private key", "TXT", "Signature block"),
                accentColor = Teal80
            )

            SectionCard(
                title = "Step 1 - Private Key",
                helper =
                    if (viewModel.privateKeyPem != null) viewModel.privateKeyLabel
                    else "Import the private key that will be used to produce the digital signature.",
                icon = Icons.Rounded.VpnKey,
                badge = "Credentials",
                accentColor = Blue80
            ) {
                if (viewModel.privateKeyPem != null) {
                    InfoPill(text = "Key imported", accentColor = Blue80)
                }
                OutlinedButton(
                    onClick = { pickKeyLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Import Private Key...")
                }
            }

            SectionCard(
                title = "Step 2 - Document",
                helper =
                    if (viewModel.documentContent != null) viewModel.documentName
                    else "Select the plain text file that will receive the appended signature.",
                icon = Icons.Rounded.Description,
                badge = "Payload",
                accentColor = Indigo80
            ) {
                if (viewModel.documentContent != null) {
                    InfoPill(text = "Document loaded", accentColor = Indigo80)
                }
                OutlinedButton(
                    onClick = { pickDocLauncher.launch(arrayOf("text/plain", "*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select .txt Document...")
                }
            }

            SectionCard(
                title = "Step 3 - Sign and Save",
                helper = "Generate the signature and export the final signed artifact when ready.",
                icon = Icons.Rounded.Save,
                badge = "Execution",
                accentColor = Teal80
            ) {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.Default) {
                            viewModel.signDocument()
                        }
                    },
                    enabled = viewModel.privateKeyPem != null && viewModel.documentContent != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Document")
                }

                if (viewModel.signedContent != null) {
                    InfoPill(text = "Signed output ready", accentColor = Teal80)
                    OutlinedButton(
                        onClick = {
                            val suggested = "signed_${viewModel.documentName.substringAfterLast('/')}"
                            saveDocLauncher.launch(suggested)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Signed Document...")
                    }
                }
            }

            StatusBanner(
                message = viewModel.statusMessage,
                isError = viewModel.isError
            )
        }
    }
}
