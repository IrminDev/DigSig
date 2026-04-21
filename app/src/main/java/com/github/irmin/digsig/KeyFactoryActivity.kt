package com.github.irmin.digsig

import android.content.Context
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.irmin.digsig.ui.theme.DigSigTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.spec.ECGenParameterSpec

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

	var statusMessage by remember { mutableStateOf("") }
	var isError by remember { mutableStateOf(false) }
	var privateKeyPem by remember { mutableStateOf<String?>(null) }
	var publicKeyPem by remember { mutableStateOf<String?>(null) }

	val savePrivKeyLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.CreateDocument("application/octet-stream")
	) { uri ->
		uri?.let {
			val pem = privateKeyPem ?: return@let
			context.contentResolver.openOutputStream(it)?.use { os -> os.write(pem.toByteArray()) }
			statusMessage = "Private key exported successfully."
			isError = false
		}
	}

	val savePubKeyLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.CreateDocument("application/octet-stream")
	) { uri ->
		uri?.let {
			val pem = publicKeyPem ?: return@let
			context.contentResolver.openOutputStream(it)?.use { os -> os.write(pem.toByteArray()) }
			statusMessage = "Public key exported successfully."
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
			text = stringResource(id = R.string.key_factory_title),
			style = MaterialTheme.typography.headlineSmall
		)

		Text(
			text = stringResource(id = R.string.key_factory_description),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Button(
			onClick = {
				scope.launch(Dispatchers.IO) {
					try {
						val kpg = KeyPairGenerator.getInstance("EC")
						kpg.initialize(ECGenParameterSpec("secp256r1"))
						val kp = kpg.generateKeyPair()

						val privPem = buildPem("PRIVATE KEY", kp.private.encoded)
						val pubPem  = buildPem("PUBLIC KEY",  kp.public.encoded)

						context.openFileOutput("key.priv", Context.MODE_PRIVATE)
							.use { it.write(privPem.toByteArray()) }
						context.openFileOutput("key.pub", Context.MODE_PRIVATE)
							.use { it.write(pubPem.toByteArray()) }

						privateKeyPem = privPem
						publicKeyPem  = pubPem
						statusMessage = "✓ Key pair generated and saved to internal storage."
						isError = false
					} catch (e: Exception) {
						statusMessage = "Error: ${e.message}"
						isError = true
					}
				}
			},
			modifier = Modifier.fillMaxWidth()
		) {
			Text(text = stringResource(id = R.string.generate_keypair_button))
		}

		privateKeyPem?.let {
			OutlinedButton(
				onClick = { savePrivKeyLauncher.launch("key.priv") },
				modifier = Modifier.fillMaxWidth()
			) {
				Text(text = stringResource(id = R.string.export_private_key))
			}
		}

		publicKeyPem?.let {
			OutlinedButton(
				onClick = { savePubKeyLauncher.launch("key.pub") },
				modifier = Modifier.fillMaxWidth()
			) {
				Text(text = stringResource(id = R.string.export_public_key))
			}
		}

		Text(
			text = statusMessage,
			color = if (isError) MaterialTheme.colorScheme.error
					else MaterialTheme.colorScheme.primary,
			style = MaterialTheme.typography.bodyMedium
		)
	}
}

private fun buildPem(type: String, encoded: ByteArray): String {
	val base64 = Base64.encodeToString(encoded, Base64.NO_WRAP)
	val sb = StringBuilder()
	sb.appendLine("-----BEGIN $type-----")
	for (i in base64.indices step 64) {
		val end = if (i + 64 > base64.length) base64.length else i + 64
		sb.appendLine(base64.substring(i, end))
	}
	sb.appendLine("-----END $type-----")
	return sb.toString()
}