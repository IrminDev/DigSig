package com.github.irmin.digsig.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.irmin.digsig.ui.components.AppScreen
import com.github.irmin.digsig.ui.components.FeatureCard
import com.github.irmin.digsig.ui.components.HeroHeader
import com.github.irmin.digsig.ui.theme.Blue80
import com.github.irmin.digsig.ui.theme.DigSigTheme
import com.github.irmin.digsig.ui.theme.Indigo80
import com.github.irmin.digsig.ui.theme.Teal80

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DigSigTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainMenu(
                        onKeyGeneratorClick = {
                            startActivity(Intent(this, KeyFactoryActivity::class.java))
                        },
                        onSignDocumentClick = {
                            startActivity(Intent(this, SignDocumentActivity::class.java))
                        },
                        onVerifySignatureClick = {
                            startActivity(Intent(this, VerifySignatureActivity::class.java))
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainMenu(
    onKeyGeneratorClick: () -> Unit,
    onSignDocumentClick: () -> Unit,
    onVerifySignatureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppScreen(modifier = modifier) {
        HeroHeader(
            title = "DigSig Vault",
            subtitle = "A sleek cryptographic workspace to generate keys, sign text files, and verify authenticity with confidence.",
            icon = Icons.Rounded.Security,
            badges = listOf("ECDSA", "P-256", "TXT only"),
            accentColor = Blue80
        )

        FeatureCard(
            title = "Generate a secure key pair",
            description = "Create fresh private and public keys in PEM format and keep them ready inside your encrypted workflow.",
            onClick = onKeyGeneratorClick,
            icon = Icons.Rounded.VpnKey,
            accentColor = Color(0xFF7C8CFF),
            label = "Key infrastructure"
        )

        FeatureCard(
            title = "Sign and seal a document",
            description = "Load a private key, append a signature to a .txt file, and export the signed artifact in seconds.",
            onClick = onSignDocumentClick,
            icon = Icons.Rounded.Edit,
            accentColor = Teal80,
            label = "Authenticity"
        )

        FeatureCard(
            title = "Verify integrity instantly",
            description = "Check if a signed file is valid and whether its contents remained untouched after signing.",
            onClick = onVerifySignatureClick,
            icon = Icons.Rounded.VerifiedUser,
            accentColor = Indigo80,
            label = "Validation"
        )
    }
}
