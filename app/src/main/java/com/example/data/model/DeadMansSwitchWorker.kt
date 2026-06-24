package com.example.data.model

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date

class DeadMansSwitchWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("DeadMansSwitchWorker", "Memulai verifikasi sinkronisasi interaksi sistem digital...")
        val manager = EstateSettingsManager(applicationContext)
        val plan = manager.loadEstatePlan()

        // Selesai jika tidak ada data wasiat disimpan atau jika wasiat sudah pernah terpicu
        if (plan.secretMessage.isEmpty() || plan.isTriggered) {
            return Result.success()
        }

        // Ambil waktu server internet (NTP safe) guna memitigasi manipulasi penanggalan perangkat lokal
        var waktuSaatIni = System.currentTimeMillis()
        val networkTime = dapatkanWaktuServerInternet()
        if (networkTime != null) {
            waktuSaatIni = networkTime
            Log.d("DeadMansSwitchWorker", "Berhasil mengambil NTP waktu internet global secure: $networkTime")
        } else {
            // Cegah manipulasi mundur penanggalan hp lokal
            val lastHeartbeat = plan.lastHeartbeat
            if (waktuSaatIni < lastHeartbeat) {
                waktuSaatIni = lastHeartbeat // Safety floor dari manipulasi penanggalan
                Log.w("DeadMansSwitchWorker", "Terdeteksi kemungkinan clock tampering! Mengamankan penanggalan.")
            }
        }

        // Kalkulasi perbedaan waktu tidak aktif
        val selisihWaktuMillis = waktuSaatIni - plan.lastHeartbeat
        val hariTidakAktif = selisihWaktuMillis / (1000 * 60 * 60 * 24)

        if (hariTidakAktif >= plan.inactiveDays) {
            eksekusiWasiatDigital(manager, plan)
        }

        return Result.success()
    }

    // Melakukan pengecekan waktu berkala dari respons server publik google demi kemudahan & presisi
    private suspend fun dapatkanWaktuServerInternet(): Long? {
        return try {
            val url = URL("https://www.google.com")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            val dateHeader = connection.getHeaderField("Date")
            if (dateHeader != null) {
                Date(dateHeader).time
            } else null
        } catch (e: Exception) {
            Log.e("DeadMansSwitchWorker", "Gagal melakukan query NTP server eksternal: ${e.message}")
            null
        }
    }

    // Mendekripsi instans khusus wasiat rahasia HNWI dan mengekspornya dalam bentuk file lokal privat
    private fun eksekusiWasiatDigital(manager: EstateSettingsManager, plan: EstatePlan) {
        manager.setTriggerStatus(true)
        Log.e("DeadMansSwitchWorker", "DEAD MAN'S SWITCH TERPICU! Pemilik terdeteksi tidak aktif selama ${plan.inactiveDays} hari.")
        
        try {
            val cacheDir = applicationContext.cacheDir
            val fileWasiat = File(cacheDir, "VELORA_SECRET_WILL_DECRYPTED.txt")
            val outputWasiat = """
                ==========================================================
                ⚠️ DEKRIPSI OTOMATIS INSTANS WASIAT PRIVATE WEALTH VELORA ⚠️
                ==========================================================
                
                Rencana warisan digital ini otomatis terpicu karena klien pemilik
                tidak melakukan interaksi login/heartbeat selama ${plan.inactiveDays} hari.
                
                INFORMASI DITERUSKAN KEPADA AHLI WARIS SAH:
                Nama Ahli Waris: ${plan.heirName}
                Kontak Verifikasi: ${plan.heirContact}
                
                ISI WASIAT RAHASIA / DIGITAL ASSETS DECRYPTION PASSWORD:
                ----------------------------------------------------------
                ${plan.secretMessage}
                ----------------------------------------------------------
                
                ==========================================================
                Keamanan didekripsi sepenuhnya pada: ${Date()}
                ==========================================================
            """.trimIndent()
            
            fileWasiat.writeText(outputWasiat)
            Log.d("DeadMansSwitchWorker", "Arsip wasiat rahasia berhasil didekripsi & disimpan di: ${fileWasiat.absolutePath}")
        } catch (e: Exception) {
            Log.e("DeadMansSwitchWorker", "Kritis: Kegagalan eksportasi wasiat digital: ${e.message}", e)
        }
    }
}
