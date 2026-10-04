package com.emergent.posapp

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// Holds the PC's LAN IP + port the backend runs on. Editable from the in-app
// Settings screen (mirrors the "Change Server" action in the WebView
// companion app), so the same build works on any WiFi network without
// recompiling. This app variant talks to the Spring Boot backend
// (springboot-backend/), which defaults to port 8081 — different from
// android-native's FastAPI backend (port 8000) — so both backends/apps can
// run side-by-side on the same PC/phone.
object ServerConfig {
    private const val PREFS_NAME = "emergent_pos_native_prefs"
    private const val KEY_SERVER_HOST = "server_host"
    private const val KEY_SERVER_PORT = "server_port"
    private const val DEFAULT_HOST = "192.168.1.45"
    private const val DEFAULT_PORT = "8081"

    lateinit var appContext: Context

    private fun prefs() = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getHost(): String = prefs().getString(KEY_SERVER_HOST, DEFAULT_HOST) ?: DEFAULT_HOST

    fun setHost(host: String) {
        prefs().edit().putString(KEY_SERVER_HOST, host.trim()).apply()
    }

    fun getPort(): String = prefs().getString(KEY_SERVER_PORT, DEFAULT_PORT) ?: DEFAULT_PORT

    fun setPort(port: String) {
        prefs().edit().putString(KEY_SERVER_PORT, port.trim()).apply()
    }

    fun apiBase(): String = "http://${getHost()}:${getPort()}"
}

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Failure(val message: String) : ApiResult<Nothing>()
}

// Thin client for the Spring Boot backend's (springboot-backend/) generic
// key-value /api/config store — the identical contract the FastAPI backend
// and the React web app (frontend/src/lib/apiClient.js) use — so this native
// app shares the exact same data (items, parties, invoices, etc.) as the web
// app and the other native app, live, through the same Postgres database.
object ApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun <T> getConfig(key: String, serializer: KSerializer<T>, default: T): T =
        withContext(Dispatchers.IO) {
            try {
                val url = "${ServerConfig.apiBase()}/api/config/$key"
                val request = Request.Builder().url(url).get().build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext default
                    val body = response.body?.string() ?: return@withContext default
                    val outer = json.parseToJsonElement(body).jsonObject
                    val configValue = outer["config_value"]?.jsonPrimitive?.content
                        ?: return@withContext default
                    json.decodeFromString(serializer, configValue)
                }
            } catch (e: Exception) {
                default
            }
        }

    suspend fun <T> setConfig(key: String, value: T, serializer: KSerializer<T>): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val configValueStr = json.encodeToString(serializer, value)
                val payload = buildJsonObject {
                    put("config_key", key)
                    put("config_value", configValueStr)
                }
                val body = payload.toString().toRequestBody("application/json".toMediaType())
                val url = "${ServerConfig.apiBase()}/api/config"
                val request = Request.Builder().url(url).post(body).build()
                client.newCall(request).execute().use { it.isSuccessful }
            } catch (e: Exception) {
                false
            }
        }

    // Raw (untyped) variants used by the backup export/import feature — we
    // want to shuttle each key's JSON value verbatim without needing a
    // matching @Serializable class for every entity.
    suspend fun getConfigRaw(key: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "${ServerConfig.apiBase()}/api/config/$key"
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val outer = json.parseToJsonElement(body).jsonObject
                outer["config_value"]?.jsonPrimitive?.content
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun setConfigRaw(key: String, rawJsonValue: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = buildJsonObject {
                put("config_key", key)
                put("config_value", rawJsonValue)
            }
            val body = payload.toString().toRequestBody("application/json".toMediaType())
            val url = "${ServerConfig.apiBase()}/api/config"
            val request = Request.Builder().url(url).post(body).build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun checkHealth(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url("${ServerConfig.apiBase()}/health").get().build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    // Matches the /api/ledger-qna, /api/reorder-advisor, /api/whatsapp-draft
    // endpoints called from frontend/src/features/ai/AIAssistantPanel.jsx.
    suspend fun postJsonForField(path: String, payload: Map<String, String>, resultField: String): ApiResult<String> =
        withContext(Dispatchers.IO) {
            try {
                val jsonObj = buildJsonObject { payload.forEach { (k, v) -> put(k, v) } }
                val body = jsonObj.toString().toRequestBody("application/json".toMediaType())
                val url = "${ServerConfig.apiBase()}$path"
                val request = Request.Builder().url(url).post(body).build()
                client.newCall(request).execute().use { response ->
                    val text = response.body?.string() ?: ""
                    if (!response.isSuccessful) return@withContext ApiResult.Failure("HTTP ${response.code}: $text")
                    val obj = json.parseToJsonElement(text).jsonObject
                    val value = obj[resultField]?.jsonPrimitive?.content ?: text
                    ApiResult.Success(value)
                }
            } catch (e: Exception) {
                ApiResult.Failure(e.message ?: "Unknown error")
            }
        }
}
