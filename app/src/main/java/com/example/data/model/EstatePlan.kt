package com.example.data.model

import android.content.Context
import android.util.Base64
import java.nio.charset.StandardCharsets

// Objek data model plan warisan digital estate planning
data class EstatePlan(
    val heirName: String,
    val heirContact: String,
    val secretMessage: String,
    val inactiveDays: Int,
    val lastHeartbeat: Long,
    val isTriggered: Boolean
)

class EstateSettingsManager(context: Context) {
    // Penyimpanan lokal privat terisolasi SharedPreferences untuk mengamankan sertifikat wasiat
    private val prefs = context.getSharedPreferences("velora_secure_estate_vault", Context.MODE_PRIVATE)

    // Menyimpan rencana estate dan mengenkripsi base64 data sensitif
    fun saveEstatePlan(heirName: String, heirContact: String, secretMessage: String, inactiveDays: Int) {
        val encryptedMsg = encryptSimple(secretMessage)
        prefs.edit().apply {
            putString("heir_name", heirName)
            putString("heir_contact", heirContact)
            putString("secret_message", encryptedMsg)
            putInt("inactive_days", inactiveDays)
            putLong("last_heartbeat", System.currentTimeMillis())
            putBoolean("is_triggered", false)
            apply()
        }
    }

    // Perbarui penanda waktu interaksi terakhir (heartbeat clock sync)
    fun updateHeartbeat() {
        prefs.edit().putLong("last_heartbeat", System.currentTimeMillis()).apply()
    }

    fun getHeartbeat(): Long = prefs.getLong("last_heartbeat", System.currentTimeMillis())

    fun loadEstatePlan(): EstatePlan {
        val encryptedMsg = prefs.getString("secret_message", "") ?: ""
        val decryptedMsg = decryptSimple(encryptedMsg)
        return EstatePlan(
            heirName = prefs.getString("heir_name", "") ?: "",
            heirContact = prefs.getString("heir_contact", "") ?: "",
            secretMessage = decryptedMsg,
            inactiveDays = prefs.getInt("inactive_days", 90),
            lastHeartbeat = prefs.getLong("last_heartbeat", System.currentTimeMillis()),
            isTriggered = prefs.getBoolean("is_triggered", false)
        )
    }

    fun setTriggerStatus(triggered: Boolean) {
        prefs.edit().putBoolean("is_triggered", triggered).apply()
    }

    // Enkripsi byte array sandi sederhana (XOR camouflaged) + Base64 agar aman dari scanning
    private fun encryptSimple(plainText: String): String {
        if (plainText.isEmpty()) return ""
        val key = 0xAA.toByte()
        val rawBytes = plainText.toByteArray(StandardCharsets.UTF_8)
        val encrypted = ByteArray(rawBytes.size)
        for (i in rawBytes.indices) {
            encrypted[i] = (rawBytes[i].toInt() xor key.toInt()).toByte()
        }
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    private fun decryptSimple(cipherText: String): String {
        if (cipherText.isEmpty()) return ""
        return try {
            val key = 0xAA.toByte()
            val rawBytes = Base64.decode(cipherText, Base64.NO_WRAP)
            val decrypted = ByteArray(rawBytes.size)
            for (i in rawBytes.indices) {
                decrypted[i] = (rawBytes[i].toInt() xor key.toInt()).toByte()
            }
            String(decrypted, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }
}
