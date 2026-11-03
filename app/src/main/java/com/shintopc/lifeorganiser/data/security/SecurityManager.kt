package com.shintopc.lifeorganiser.data.security

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.MessageDigest

class SecurityManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "secure_app_prefs"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }

    private val sharedPreferences by lazy {
        /*
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Throwable) {
            e.printStackTrace()
            // Fallback to standard SharedPreferences if encryption fails (e.g., emulator issues, old OS)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        */
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isPinSet(): Boolean {
        return sharedPreferences.contains(KEY_PIN_HASH)
    }

    fun savePin(pin: String) {
        val hash = hashPin(pin)
        sharedPreferences.edit()
            .putString(KEY_PIN_HASH, hash)
            .apply()
    }

    fun validatePin(pin: String): Boolean {
        val storedHash = sharedPreferences.getString(KEY_PIN_HASH, null) ?: return false
        return storedHash == hashPin(pin)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
            .apply()
    }

    fun isBiometricEnabled(): Boolean {
        // Default to true if user hasn't explicitly disabled it, or prompt to enable flow
        // For this implementation, we'll default to false until user enables
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun clearClipboard() {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                // Clears the clipboard without triggering the "Copied" notification on Android 13+
                clipboard.clearPrimaryClip()
            } else {
                // Fallback for older versions
                val clip = ClipData.newPlainText("", "")
                clipboard.setPrimaryClip(clip)
            }
        } catch (e: Exception) {
            // Ignore background access restrictions or other failures
            e.printStackTrace()
        }
    }

    private fun hashPin(pin: String): String {
        val bytes = pin.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
