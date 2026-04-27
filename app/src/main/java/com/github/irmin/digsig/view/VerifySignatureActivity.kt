package com.github.irmin.digsig.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.VerifiedUser
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
import com.github.irmin.digsig.viewmodel.VerifySignatureViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VerifySignatureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DigSigTheme {
                Scaffold { innerPadding ->
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
    val viewModel = remember { VerifySignatureViewModel() }

    LaunchedEffect(Unit) {
        viewModel.loadInternalPublicKey(context)
    }

    val pickKeyLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importPublicKey(context, it) }
    }

    val pickDocLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importDocument(context, it) }
    }

    AppScreen(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HeroHeader(
                title = "Verify a Signature",
                subtitle = "Inspect a signed text file, validate its cryptographic proof, and detect tampering instantly.",
                icon = Icons.Rounded.VerifiedUser,
                badges = listOf("Public key", "Integrity", "Trust"),
                accentColor = Indigo80
            )

            SectionCard(
                title = "Step 1 - Public Key",
                helper =
                    if (viewModel.publicKeyPem != null) viewModel.publicKeyLabel
                    else "Import the matching public key to validate the signer identity.",
                icon = Icons.Rounded.VpnKey,
                badge = "Credentials",
                accentColor = Blue80
            ) {
                if (viewModel.publicKeyPem != null) {
                    InfoPill(text = "Public key loaded", accentColor = Blue80)
                }
                OutlinedButton(
                    onClick = { pickKeyLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Import Public Key...")
                }
            }

            SectionCard(
                title = "Step 2 - Signed Document",
                helper =
                    if (viewModel.documentContent != null) viewModel.documentName
                    else "Select the signed .txt document whose authenticity you want to inspect.",
                icon = Icons.Rounded.Description,
                badge = "Evidence",
                accentColor = Teal80
            ) {
                if (viewModel.documentContent != null) {
                    InfoPill(text = "Signed file loaded", accentColor = Teal80)
                }
                OutlinedButton(
                    onClick = { pickDocLauncher.launch(arrayOf("text/plain", "*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select Signed Document...")
                }
            }

            SectionCard(
                title = "Step 3 - Validate",
                helper = "Run cryptographic verification and review the trust result below.",
                icon = Icons.Rounded.VerifiedUser,
                badge = "Verification",
                accentColor = Indigo80
            ) {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.Default) {
                            viewModel.verify()
                        }
                    },
                    enabled = viewModel.publicKeyPem != null && viewModel.documentContent != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verify Signature")
                }
            }

            StatusBanner(
                message = viewModel.statusMessage,
                isError = viewModel.isError
            )
        }
    }
}
