package com.shintopc.lifeorganiser.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import android.util.Base64

class CryptoManager {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private val encryptCipher get() = Cipher.getInstance(TRANSFORMATION).apply {
        init(Cipher.ENCRYPT_MODE, getKey())
    }

    private fun getDecryptCipherForIv(iv: ByteArray): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
        }
    }

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry("secret_app_key", null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(
                    "secret_app_key",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }

    fun encrypt(bytes: ByteArray, outputStream: OutputStream): ByteArray {
        val cipher = encryptCipher
        val iv = cipher.iv
        outputStream.write(iv) // Write IV first
        val encryptedBytes = cipher.doFinal(bytes)
        outputStream.write(encryptedBytes)
        return iv
    }

    fun decrypt(inputStream: InputStream): ByteArray {
        return inputStream.use { input ->
            // AES/CBC/PKCS7Padding usually has 16 byte block size. Let's check params.
            // Using CBC here.
            val ivSize = 16 
            val ivBytes = ByteArray(ivSize)
            val bytesRead = input.read(ivBytes)
            
            if (bytesRead < ivSize) {
                 return byteArrayOf() // Fail or empty?
            }

            val cipher = getDecryptCipherForIv(ivBytes)
            val encryptedBytes = input.readBytes()
            cipher.doFinal(encryptedBytes)
        }
    }
    
    // String Helpers
    fun encrypt(text: String): String {
        val cipher = encryptCipher
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
        
        // combine IV and Encrypted Bytes
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
        
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }
    
    fun decrypt(text: String): String {
        try {
            val combined = Base64.decode(text, Base64.DEFAULT)
            
            val iv = ByteArray(16)
            System.arraycopy(combined, 0, iv, 0, iv.size)
            
            val encryptedBytes = ByteArray(combined.size - 16)
            System.arraycopy(combined, 16, encryptedBytes, 0, encryptedBytes.size)
            
            val cipher = getDecryptCipherForIv(iv)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            return text // Fallback to returning raw text if decryption fails (e.g. not encrypted yet)
        }
    }

    companion object {
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}
