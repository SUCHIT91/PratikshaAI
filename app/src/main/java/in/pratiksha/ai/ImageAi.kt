package `in`.pratiksha.ai

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

internal fun callAiWithImage(
    endpoint: String,
    model: String,
    key: String?,
    question: String,
    memories: String,
    bitmap: Bitmap
): String {
    if (endpoint.isBlank() || model.isBlank() || key.isNullOrBlank()) {
        return "त्रुटि: पहले Settings में API endpoint, model और API key भरें।"
    }

    var connection: HttpURLConnection? = null

    return try {
        val resized = Bitmap.createScaledBitmap(
            bitmap,
            minOf(bitmap.width, 1024),
            minOf(bitmap.height, 1024),
            true
        )

        val output = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 80, output)
        if (resized !== bitmap) resized.recycle()

        val imageData = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        val system = "You are Pratiksha AI, a helpful personal assistant. " +
            "Analyze the user's image carefully. Reply in Hindi or English matching the user's language. " +
            "Do not claim details that cannot be seen. User memories: $memories"

        val content = JSONArray()
            .put(JSONObject().put("type", "text").put("text", question))
            .put(
                JSONObject()
                    .put("type", "image_url")
                    .put(
                        "image_url",
                        JSONObject().put("url", "data:image/jpeg;base64,$imageData")
                    )
            )

        val messages = JSONArray()
            .put(JSONObject().put("role", "system").put("content", system))
            .put(JSONObject().put("role", "user").put("content", content))

        val body = JSONObject()
            .put("model", model)
            .put("messages", messages)
            .put("temperature", 0.5)

        connection = URL(endpoint).openConnection() as HttpURLConnection
        connection!!.requestMethod = "POST"
        connection!!.connectTimeout = 20000
        connection!!.readTimeout = 60000
        connection!!.doOutput = true
        connection!!.setRequestProperty("Content-Type", "application/json")
        connection!!.setRequestProperty("Authorization", "Bearer $key")

        connection!!.outputStream.use {
            it.write(body.toString().toByteArray(Charsets.UTF_8))
        }

        val code = connection!!.responseCode
        val stream = if (code in 200..299) connection!!.inputStream
                     else connection!!.errorStream
        val raw = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

        if (code !in 200..299) {
            val detail = runCatching {
                JSONObject(raw).optJSONObject("error")?.optString("message")
            }.getOrNull()
            "त्रुटि: AI server ने HTTP $code दिया। ${detail ?: raw.take(180)}"
        } else {
            val message = JSONObject(raw)
                .optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")

            val answer = message?.optString("content").orEmpty()
            if (answer.isNotBlank() && answer != "null") answer
            else "त्रुटि: AI से जवाब नहीं मिला।"
        }
    } catch (e: Exception) {
        "त्रुटि: ${e.localizedMessage ?: "फोटो का विश्लेषण नहीं हो पाया।"}"
    } finally {
        connection?.disconnect()
    }
}
