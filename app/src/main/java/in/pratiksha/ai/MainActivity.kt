package in.pratiksha.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

// ---------- Colors (har naam sirf EK baar) ----------
private val PkNight = Color(0xFF090916)
private val PkPanel = Color(0xFF17152B)
private val PkViolet = Color(0xFF955BFF)
private val PkLavender = Color(0xFFC9A8FF)
private val PkMuted = Color(0xFF9990B8)
private val PkWhite = Color(0xFFF8F5FF)

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        setContent { PratikshaApp(onSpeak = { speak(it) }) }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts?.language = Locale("hi", "IN")
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "pratiksha-reply")
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}

private data class ChatMessage(val role: String, val text: String)

@Composable
private fun PratikshaApp(onSpeak: (String) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pratiksha", Activity.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    var page by remember { mutableStateOf("Home") }
    var input by remember { mutableStateOf("") }
    var endpoint by remember {
        mutableStateOf(prefs.getString("endpoint", "https://api.openai.com/v1/chat/completions") ?: "")
    }
    var model by remember { mutableStateOf(prefs.getString("model", "gpt-4o-mini") ?: "") }
    var apiKey by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var energy by remember { mutableIntStateOf(prefs.getInt("energy", 2)) }
    val mood by remember { mutableStateOf(prefs.getString("mood", "अपना मूड चुनें") ?: "अपना मूड चुनें") }
    var memoryText by remember { mutableStateOf(prefs.getString("memories", "") ?: "") }
    val messages = remember {
        mutableStateListOf<ChatMessage>().apply {
            val raw = prefs.getString("history", "[]") ?: "[]"
            runCatching {
                val arr = JSONArray(raw)
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(ChatMessage(o.optString("role"), o.optString("text")))
                }
            }
        }
    }

    // Ek hi jagah se message bhejne ka logic (Home aur Chat dono use karte hain)
    val sendMessage: () -> Unit = {
        val question = input.trim()
        if (question.isNotEmpty() && !busy) {
            input = ""
            messages.add(ChatMessage("user", question))
            saveHistory(prefs, messages)
            page = "Chat"
            busy = true
            status = ""
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    callAi(
                        endpoint,
                        model,
                        SecretVault.decrypt(prefs.getString("api_key_enc", null)),
                        question,
                        memoryText
                    )
                }
                messages.add(ChatMessage("assistant", result))
                saveHistory(prefs, messages)
                busy = false
                if (result.startsWith("त्रुटि:")) status = result
            }
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                input = spoken
                page = "Chat"
            }
        }
    }

    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "प्रतिक्षा से बोलिए")
            }
            runCatching { speechLauncher.launch(intent) }
                .onFailure { status = "इस फोन पर speech recognition उपलब्ध नहीं है।" }
        } else {
            status = "Voice input के लिए microphone permission चाहिए।"
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = PkViolet,
            secondary = PkLavender,
            background = PkNight,
            surface = PkPanel,
            onBackground = PkWhite,
            onSurface = PkWhite
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = PkNight) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFF0B0A18), PkNight, Color(0xFF121027))))
            ) {
                TopBar(
                    onMenu = { page = if (page == "Settings") "Home" else "Settings" },
                    onBell = { status = "अभी कोई notification नहीं है।" }
                )
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (page) {
                        "Home" -> HomePage(
                            energy = energy,
                            mood = mood,
                            onEnergy = {
                                energy = (energy + 1) % 10
                                prefs.edit().putInt("energy", energy).apply()
                            },
                            onPage = { page = it },
                            status = status,
                            input = input,
                            onInput = { input = it },
                            onSend = sendMessage,
                            onMic = { micPermission.launch(Manifest.permission.RECORD_AUDIO) }
                        )

                        "Chat" -> ChatPage(
                            messages = messages,
                            busy = busy,
                            input = input,
                            onInput = { input = it },
                            onSend = sendMessage,
                            onSpeak = onSpeak,
                            status = status
                        )

                        "Settings" -> SettingsPage(
                            endpoint = endpoint,
                            onEndpoint = { endpoint = it },
                            model = model,
                            onModel = { model = it },
                            apiKey = apiKey,
                            onApiKey = { apiKey = it },
                            onSave = {
                                prefs.edit()
                                    .putString("endpoint", endpoint.trim())
                                    .putString("model", model.trim())
                                    .putString("api_key_enc", SecretVault.encrypt(apiKey.trim()))
                                    .apply()
                                apiKey = ""
                                status = "सेटिंग्स सेव हो गईं।"
                            },
                            status = status
                        )

                        "Yaadein" -> MemoriesPage(
                            text = memoryText,
                            onText = { memoryText = it },
                            onSave = {
                                prefs.edit().putString("memories", memoryText).apply()
                                status = "यादें सेव हो गईं।"
                            },
                            status = status
                        )

                        "Scan" -> ScanPage(
                            onLaunchCamera = {
                                val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    runCatching { context.startActivity(intent) }
                                        .onFailure { status = "Camera नहीं खुल पाया।" }
                                } else {
                                    status = "इस डिवाइस पर camera app उपलब्ध नहीं है।"
                                }
                            },
                            status = status
                        )

                        else -> SimplePage(page, status) { page = "Home" }
                    }
                }
                BottomBar(
                    page = page,
                    onNavigate = { page = it },
                    onMic = { micPermission.launch(Manifest.permission.RECORD_AUDIO) }
                )
            }
        }
    }
}

private fun saveHistory(prefs: android.content.SharedPreferences, messages: List<ChatMessage>) {
    val arr = JSONArray()
    messages.takeLast(100).forEach {
        arr.put(JSONObject().put("role", it.role).put("text", it.text))
    }
    prefs.edit().putString("history", arr.toString()).apply()
}

private fun callAi(endpoint: String, model: String, key: String?, question: String, memories: String): String {
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
            .put(JSONObject().put("role", "user").put("content", question))
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

private object SecretVault {
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

// ---------- UI ----------

@Composable
private fun TopBar(onMenu: () -> Unit, onBell: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenu) { Icon(Icons.Default.Menu, contentDescription = "Menu", tint = PkWhite) }
        Text(
            "Pratiksha",
            Modifier.weight(1f),
            color = PkWhite,
            fontSize = 23.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        IconButton(onClick = onBell) { Text("🔔", fontSize = 20.sp) }
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(PkViolet, Color(0xFF21133D)))),
            contentAlignment = Alignment.Center
        ) {
            Text("P", color = PkWhite, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
    }
}

@Composable
private fun HomePage(
    energy: Int,
    mood: String,
    onEnergy: () -> Unit,
    onPage: (String) -> Unit,
    status: String,
    input: String,
    onInput: (String) -> Unit,
    onSend: () -> Unit,
    onMic: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.weight(1f)) {
                Text("Good morning,", color = PkMuted, fontSize = 20.sp)
                Text("Suchit Bhai ✨", color = PkWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = PkPanel),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.clickable { onEnergy() }
            ) {
                Column(
                    
