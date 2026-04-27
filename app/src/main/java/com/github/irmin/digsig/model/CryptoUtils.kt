package com.github.irmin.digsig.model

import android.util.Base64
import java.security.KeyPairGenerator
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec
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

data class GeneratedKeys(val privateKeyPem: String, val publicKeyPem: String)

fun generateEcKeyPairPem(): GeneratedKeys {
    val kpg = KeyPairGenerator.getInstance("EC")
    kpg.initialize(ECGenParameterSpec("secp256r1"))
    val kp = kpg.generateKeyPair()
    return GeneratedKeys(
        privateKeyPem = buildPem("PRIVATE KEY", kp.private.encoded),
        publicKeyPem = buildPem("PUBLIC KEY", kp.public.encoded)
    )
}

fun signTextDocument(privateKeyPem: String, documentContent: String): String {
    val original = stripSignature(documentContent)
    val privateKey = parsePemPrivateKey(privateKeyPem)
    val signer = Signature.getInstance("SHA256withECDSA")
    signer.initSign(privateKey)
    signer.update(original.toByteArray(Charsets.UTF_8))
    val sigB64 = Base64.encodeToString(signer.sign(), Base64.NO_WRAP)
    return original + SIG_HEADER + sigB64 + SIG_FOOTER
}

fun verifySignedTextDocument(publicKeyPem: String, documentContent: String): Boolean {
    if (!documentContent.contains(SIG_HEADER) || !documentContent.contains(SIG_FOOTER)) {
        return false
    }
    val original = documentContent.substringBefore(SIG_HEADER)
    val sigB64 = documentContent
        .substringAfter(SIG_HEADER)
        .substringBefore(SIG_FOOTER)
        .trim()
    val sigBytes = Base64.decode(sigB64, Base64.DEFAULT)
    val publicKey = parsePemPublicKey(publicKeyPem)
    val verifier = Signature.getInstance("SHA256withECDSA")
    verifier.initVerify(publicKey)
    verifier.update(original.toByteArray(Charsets.UTF_8))
    return verifier.verify(sigBytes)
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



