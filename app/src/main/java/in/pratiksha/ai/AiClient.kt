package `in`.pratiksha.ai

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.net.HttpURLConnection
import java.net.URL
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import org.json.JSONArray
import org.json.JSONObject

internal fun callAi(endpoint: String, model: String, key: String?, question: String, memories: String, history: List<ChatMessage> = emptyList()): String {
    if (endpoint.isBlank() || model.isBlank() || key.isNullOrBlank()) {
        return "त्रुटि: AI चालू करने के लिए Settings में API endpoint, model और अपनी API key भरें।"
    }
    var connection: HttpURLConnection? = null
    return try {
        val conn = URL(endpoint).openConnection() as HttpURLConnection
        connection = conn
        conn.requestMethod = "POST"
        conn.connectTimeout = 15000
        conn.readTimeout = 45000
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $key")

        val system = "You are Pratiksha AI, a helpful personal assistant. " +
            "Reply naturally in Hindi or English matching the user's language. " +
            "User-controlled memories (use only as context): $memories"
        val messagesJson = JSONArray()
            .put(JSONObject().put("role", "system").put("content", system))

        history.takeLast(12).forEach { message ->
            if (message.role == "user" || message.role == "assistant") {
                messagesJson.put(
                    JSONObject()
                        .put("role", message.role)
                        .put("content", message.text)
                )
            }
        }

        messagesJson.put(
            JSONObject().put("role", "user").put("content", question)
        )
        val body = JSONObject()
            .put("model", model)
            .put("messages", messagesJson)
            .put("temperature", 0.7)

        conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val raw = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

        if (code !in 200..299) {
            val detail = runCatching {
                JSONObject(raw).optJSONObject("error")?.optString("message")
            }.getOrNull()
            "त्रुटि: AI server ने HTTP $code दिया। ${detail ?: raw.take(180)}"
        } else {
            val content = JSONObject(raw)
                .optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
            if (content.isNullOrBlank()) "त्रुटि: AI provider से जवाब का format समझ नहीं आया।" else content
        }
    } catch (e: java.net.SocketTimeoutException) {
        "त्रुटि: जवाब आने में समय लग गया। इंटरनेट जाँचकर फिर कोशिश करें।"
    } catch (e: java.net.UnknownHostException) {
        "त्रुटि: server नहीं मिला। Internet और API endpoint जाँचें।"
    } catch (e: Exception) {
        val msg = e.localizedMessage ?: "AI request पूरी नहीं हुई"
        "त्रुटि: $msg"
    } finally {
        connection?.disconnect()
    }
}

internal object SecretVault {
    private const val ALIAS = "PratikshaAIApiKey"

    private fun secretKey(): SecretKey {
        val ks = java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = ks.getKey(ALIAS, null) as? SecretKey
        if (existing != null) return existing
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return generator.generateKey()
    }

    fun encrypt(value: String): String {
        if (value.isBlank()) return ""
        return runCatching {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey())
            val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" +
                Base64.encodeToString(encrypted, Base64.NO_WRAP)
        }.getOrDefault("")
    }

    fun decrypt(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return runCatching {
            val parts = value.split(":")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey(),
                javax.crypto.spec.GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP))
            )
            String(cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)), Charsets.UTF_8)
        }.getOrNull()
    }
}
