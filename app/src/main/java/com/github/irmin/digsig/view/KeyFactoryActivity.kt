package com.github.irmin.digsig.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.irmin.digsig.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.irmin.digsig.ui.components.AppScreen
import com.github.irmin.digsig.ui.components.HeroHeader
import com.github.irmin.digsig.ui.components.InfoPill
import com.github.irmin.digsig.ui.components.SectionCard
import com.github.irmin.digsig.ui.components.StatusBanner
import com.github.irmin.digsig.ui.theme.Blue80
import com.github.irmin.digsig.ui.theme.DigSigTheme
import com.github.irmin.digsig.ui.theme.Teal80
import com.github.irmin.digsig.viewmodel.KeyFactoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KeyFactoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DigSigTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    KeyFactoryScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun KeyFactoryScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel = remember { KeyFactoryViewModel() }

    val savePrivKeyLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            val pem = viewModel.privateKeyPem ?: return@let
            context.contentResolver.openOutputStream(it)?.use { os -> os.write(pem.toByteArray()) }
            viewModel.onPrivateKeyExported()
        }
    }

    val savePubKeyLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            val pem = viewModel.publicKeyPem ?: return@let
            context.contentResolver.openOutputStream(it)?.use { os -> os.write(pem.toByteArray()) }
            viewModel.onPublicKeyExported()
        }
    }

    AppScreen(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeroHeader(
                title = stringResource(id = R.string.key_pair_generator),
                subtitle = "Forge a fresh cryptographic identity for signing and verification, stored in PEM format.",
                icon = Icons.Rounded.VpnKey,
                badges = listOf("Local vault", "PKCS#8", "X.509"),
                accentColor = Blue80
            )

            SectionCard(
                title = "Generate",
                helper = stringResource(id = R.string.key_factory_description),
                icon = Icons.Rounded.Key,
                badge = "Step 1",
                accentColor = Blue80
            ) {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            viewModel.generateAndStoreKeyPair(context)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.generate_keypair_button))
                }
            }

            if (viewModel.privateKeyPem != null || viewModel.publicKeyPem != null) {
                SectionCard(
                    title = "Export Keys",
                    helper = "Back up your PEM files or move them to another trusted device when required.",
                    icon = Icons.Rounded.Share,
                    badge = "Step 2",
                    accentColor = Teal80
                ) {
                    if (viewModel.privateKeyPem != null) {
                        InfoPill(text = "Private key ready", accentColor = Blue80)
                    }
                    if (viewModel.publicKeyPem != null) {
                        InfoPill(text = "Public key ready", accentColor = Teal80)
                    }
                    if (viewModel.privateKeyPem != null) {
                        OutlinedButton(
                            onClick = { savePrivKeyLauncher.launch("key.priv") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stringResource(id = R.string.export_private_key))
                        }
                    }
                    if (viewModel.publicKeyPem != null) {
                        OutlinedButton(
                            onClick = { savePubKeyLauncher.launch("key.pub") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stringResource(id = R.string.export_public_key))
                        }
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

