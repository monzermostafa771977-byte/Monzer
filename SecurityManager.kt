package com.monzer.hares

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurityManager {
    private const val SECURE_PREFS_FILE = "hares_secure_prefs"

    private fun getEncryptedPrefs(context: Context) = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        null
    }

    fun setClipboardProtectionEnabled(context: Context, enabled: Boolean) {
        getEncryptedPrefs(context)?.edit()?.putBoolean("clipboard_protection", enabled)?.apply()
    }

    fun isClipboardProtectionEnabled(context: Context): Boolean {
        return getEncryptedPrefs(context)?.getBoolean("clipboard_protection", true) ?: true
    }
}
