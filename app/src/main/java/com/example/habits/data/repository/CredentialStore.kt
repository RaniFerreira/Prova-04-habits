package com.example.habits.data.repository

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Guarda o último login (e-mail/senha) criptografado com uma chave AES do Android Keystore,
 * para permitir "Entrar com Biometria". A biometria (BiometricPrompt) é o portão de acesso:
 * as credenciais só são usadas depois que o usuário se autentica com a digital/rosto.
 */
class CredentialStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("habitflow_secure", Context.MODE_PRIVATE)

    fun hasCredentials(): Boolean = prefs.contains(KEY_CREDENTIALS)

    fun save(email: String, password: String) {
        runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey())
            val encrypted = cipher.doFinal("$email\n$password".toByteArray(Charsets.UTF_8))
            prefs.edit { putString(KEY_CREDENTIALS, "${encode(cipher.iv)}:${encode(encrypted)}") }
        }
    }

    fun load(): Pair<String, String>? = runCatching {
        val (iv, data) = requireNotNull(prefs.getString(KEY_CREDENTIALS, null)).split(":")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, decode(iv)))
        val (email, password) = String(cipher.doFinal(decode(data)), Charsets.UTF_8).split("\n", limit = 2)
        email to password
    }.getOrNull()

    fun clear() = prefs.edit { remove(KEY_CREDENTIALS) }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return generator.generateKey()
    }

    private fun encode(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)
    private fun decode(text: String) = Base64.decode(text, Base64.NO_WRAP)

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "habitflow_credentials_key"
        const val KEY_CREDENTIALS = "credentials"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
