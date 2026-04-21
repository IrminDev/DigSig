package com.github.irmin.digsig

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.irmin.digsig.ui.theme.DigSigTheme

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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(id = R.string.main_menu_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Button(onClick = onKeyGeneratorClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(id = R.string.key_pair_generator))
        }

        Button(onClick = onSignDocumentClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(id = R.string.sign_document))
        }

        Button(onClick = onVerifySignatureClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(id = R.string.verify_signature))
        }
    }
}