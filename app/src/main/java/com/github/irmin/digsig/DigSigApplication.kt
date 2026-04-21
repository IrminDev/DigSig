package com.github.irmin.digsig

import android.app.Application
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class DigSigApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        registerBouncyCastleProvider()
    }

    private fun registerBouncyCastleProvider() {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
    }
}