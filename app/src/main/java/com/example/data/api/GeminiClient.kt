package com.example.data.api

import android.os.Build
import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun dapatkanSaranRebalancing(
        promptUser: String,
        stocksVal: Double,
        cryptoVal: Double,
        goldVal: Double
    ): GeminiResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key belum dikonfigurasi dengan benar")
            return@withContext GeminiResponse(
                intent = "ERROR_AUTH",
                parameters = mapOf("error" to "API Key kosong"),
                recommendation_text = "Maaf, API Key Gemini belum dipasang di Secrets panel. Tolong siapkan kunci di sana ya.",
                action_code = "SHOW_ERROR"
            )
        }

        val totalPortofolio = stocksVal + cryptoVal + goldVal
        val sPercent = if (totalPortofolio == 0.0) 0.0 else (stocksVal / totalPortofolio) * 100
        val cPercent = if (totalPortofolio == 0.0) 0.0 else (cryptoVal / totalPortofolio) * 100
        val gPercent = if (totalPortofolio == 0.0) 0.0 else (goldVal / totalPortofolio) * 100

        // Masukkan context alokasi aset pengguna ke dalam system systemInstruction
        val systemInstruction = """
            Anda adalah Velora AI Rebalancing Advisor, konsultan manajemen kekayaan elit untuk nasabah VIP.
            Tugas Anda menganalisis input natural language pengguna dan memberikan rekomendasi alokasi portofolio rebalancing finansial dalam Bahasa Indonesia yang santai tapi elegan.
            
            Informasi portofolio nasabah saat ini:
            - Saham (Stocks): Rp $stocksVal (${"%.1f".format(sPercent)}%)
            - Kripto (Crypto): Rp $cryptoVal (${"%.1f".format(cPercent)}%)
            - Emas (Gold): Rp $goldVal (${"%.1f".format(gPercent)}%)
            - Total Aset: Rp $totalPortofolio
            
            Anda WAJIB memberikan respon berupa JSON murni tanpa pembungkus markdown (```json atau semacamnya). Pastikan JSON-nya valid sesuai skema berikut:
            {
              "intent": "SIMULATE_REBALANCE",
              "parameters": { "amount": 20000000, "target_asset": "all" },
              "recommendation_text": "Teks rekomendasi rebalancing detail dari asisten AI dalam Bahasa Indonesia...",
              "action_code": "EXECUTE_DUMMY_INFLOW"
            }
        """.trimIndent()

        // Susun payload request manual agar super fleksibel & andal tanpa library tambahan
        val requestBodyJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", promptUser)
                        }
                        put(partObj)
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            val systemInstructionObj = JSONObject().apply {
                val partsArray = JSONArray().apply {
                    val partObj = JSONObject().apply {
                        put("text", systemInstruction)
                    }
                    put(partObj)
                }
                put("parts", partsArray)
            }
            put("systemInstruction", systemInstructionObj)

            val generationConfigObj = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.4)
            }
            put("generationConfig", generationConfigObj)
        }

        val modelCandidates = listOf(
            "gemini-3.5-flash",
            "gemini-3.1-flash-lite-preview",
            "gemini-3.1-pro-preview"
        )

        var lastErrorCode = -1
        var lastErrorMessage = ""
        var successResponse: GeminiResponse? = null

        for (model in modelCandidates) {
            val urlStr = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
            val request = Request.Builder()
                .url("$urlStr?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            for (attempt in 1..2) {
                try {
                    client.newCall(request).execute().use { response ->
                        val bodyStr = response.body?.string() ?: ""
                        if (response.isSuccessful) {
                            val mainJsonObj = JSONObject(bodyStr)
                            val candidatesArray = mainJsonObj.getJSONArray("candidates")
                            val textResponse = candidatesArray.getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text")

                            val cleanedText = textResponse.trim()
                                .removePrefix("```json")
                                .removePrefix("```")
                                .removeSuffix("```")
                                .trim()

                            val responseJson = JSONObject(cleanedText)
                            val intent = responseJson.optString("intent", "SIMULATE_REBALANCE")
                            val recommendation = responseJson.optString("recommendation_text", "Saran kalkulasi berhasil diproses.")
                            val actionCode = responseJson.optString("action_code", "EXECUTE_DUMMY_INFLOW")
                            val paramsObj = responseJson.optJSONObject("parameters")
                            val paramsMap = mutableMapOf<String, Any>()
                            if (paramsObj != null) {
                                val keys = paramsObj.keys()
                                while (keys.hasNext()) {
                                    val key = keys.next()
                                    paramsMap[key] = paramsObj.get(key)
                                }
                            }

                            successResponse = GeminiResponse(
                                intent = intent,
                                parameters = paramsMap,
                                recommendation_text = recommendation,
                                action_code = actionCode
                            )
                        } else {
                            lastErrorCode = response.code
                            lastErrorMessage = "HTTP $lastErrorCode: $bodyStr"
                            Log.w(TAG, "Model $model attempt $attempt gagal dengan HTTP $lastErrorCode, body: $bodyStr")
                            if (response.code == 400 || response.code == 403) {
                                break
                            }
                        }
                    }
                } catch (e: Exception) {
                    lastErrorMessage = e.message ?: "Exception"
                    Log.e(TAG, "Model $model attempt $attempt melempar exception: ${e.message}", e)
                }

                if (successResponse != null) break
                delay(500)
            }

            if (successResponse != null) break
        }

        if (successResponse != null) {
            successResponse!!
        } else {
            val userFriendlyMsg = if (lastErrorCode == 503) {
                "Sangat menyesal, server AI sedang mengalami beban berlebih atau kuota habis (HTTP 503). Kami merekomendasikan untuk mencoba kembali beberapa saat lagi."
            } else if (lastErrorCode == 400 || lastErrorCode == 403) {
                "Harap periksa kembali validitas API Key Gemini Anda (HTTP $lastErrorCode). Pastikan sudah benar di Secrets panel."
            } else {
                "Sistem asisten otomatis kami sedang offline atau mengalami kendala koneksi di semua model cadangan ($lastErrorMessage). Silakan periksa jaringan internet Anda."
            }

            GeminiResponse(
                intent = "ERROR_API",
                parameters = mapOf("http_code" to lastErrorCode, "error_msg" to lastErrorMessage),
                recommendation_text = userFriendlyMsg,
                action_code = "SHOW_ERROR"
            )
        }
    }
}

data class GeminiResponse(
    val intent: String,
    val parameters: Map<String, Any>,
    val recommendation_text: String,
    val action_code: String
)
