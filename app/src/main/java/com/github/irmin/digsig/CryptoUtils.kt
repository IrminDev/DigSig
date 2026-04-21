package com.github.irmin.digsig

import android.util.Base64
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

/** Delimiter that separates the original document text from the appended signature block. */
const val SIG_HEADER = "\n---BEGIN DIGITAL SIGNATURE---\n"
const val SIG_FOOTER = "\n---END DIGITAL SIGNATURE---\n"

/** Parses a PKCS#8 PEM private key (EC). */
fun parsePemPrivateKey(pem: String): PrivateKey {
    val base64 = pem
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replace("-----BEGIN EC PRIVATE KEY-----", "")
        .replace("-----END EC PRIVATE KEY-----", "")
        .replace("\\s".toRegex(), "")
    val der = Base64.decode(base64, Base64.DEFAULT)
    return KeyFactory.getInstance("EC").generatePrivate(PKCS8EncodedKeySpec(der))
}

/** Parses an X.509 PEM public key (EC). */
fun parsePemPublicKey(pem: String): PublicKey {
    val base64 = pem
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
        .replace("\\s".toRegex(), "")
    val der = Base64.decode(base64, Base64.DEFAULT)
    return KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(der))
}

/** Strips an existing signature block from a document, returning the original content. */
fun stripSignature(content: String): String =
    if (content.contains(SIG_HEADER)) content.substringBefore(SIG_HEADER) else content


