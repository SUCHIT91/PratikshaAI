package in.pratiksha.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64

private val Night = Color(0xFF090916)
private val Panel = Color(0xFF17152B)
private val Violet = Color(0xFF955BFF)
private val Lavender = Color(0xFFC9A8FF)
private val Muted = Color(0xFF9990B8)
private val White = Color(0xFFF8F5FF)

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
    private fun speak(text: String) { tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "pratiksha-reply") }
    override fun onDestroy() { tts?.shutdown(); super.onDestroy() }
}

private data class ChatMessage(val role: String, val text: String)

@Composable
private fun PratikshaApp(onSpeak: (String) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pratiksha", Activity.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    var page by remember { mutableStateOf("Home") }
    var input by remember { mutableStateOf("") }
    var endpoint by remember { mutableStateOf(prefs.getString("endpoint", "https://api.openai.com/v1/chat/completions") ?: "") }
    var model by remember { mutableStateOf(prefs.getString("model", "gpt-4o-mini") ?: "") }
    var apiKey by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var energy by remember { mutableIntStateOf(prefs.getInt("energy", 2)) }
    var mood by remember { mutableStateOf(prefs.getString("mood", "अपना मूड चुनें") ?: "अपना मूड चुनें") }
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

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) { input = spoken; page = "Chat" }
        }
    }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "प्रतिक्षा से बोलिए")
            }
            runCatching { speechLauncher.launch(intent) }.onFailure { status = "इस फोन पर speech recognition उपलब्ध नहीं है।" }
        } else status = "Voice input के लिए microphone permission चाहिए।"
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Violet, secondary = Lavender, background = Night,
        surface = Panel, onBackground = White, onSurface = White
    )) {
        Surface(Modifier.fillMaxSize(), color = Night) {
            Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0B0A18), Night, Color(0xFF121027))))) {
                TopBar(onMenu = { page = if (page == "Settings") "Home" else "Settings" }, onBell = { status = "अभी कोई notification नहीं है।" })
                Box(Modifier.weight(1f).fillMaxWidth()) {
                when (page) {
                    "Home" -> HomePage(energy, mood, onEnergy = { energy = (energy + 1) % 10; prefs.edit().putInt("energy", energy).apply() },
                        onPage = { page = it }, status = status, input = input, onInput = { input = it },
                        onSend = {
                            val question = input.trim()
                            if (question.isNotEmpty() && !busy) {
                                input = ""; messages.add(ChatMessage("user", question)); saveHistory(prefs, messages)
                                page = "Chat"; busy = true; status = ""
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        callAi(endpoint, model, SecretVault.decrypt(context, prefs.getString("api_key_enc", null)), question, memoryText)
                                    }
                                    messages.add(ChatMessage("assistant", result))
                                    saveHistory(prefs, messages)
                                    busy = false
                                    if (result.startsWith("त्रुटि:")) status = result
                                }
                            }
                        }, onMic = { micPermission.launch(Manifest.permission.RECORD_AUDIO) })
                    "Chat" -> ChatPage(messages, busy, input, { input = it }, {
                        val q = input.trim()
                        if (q.isNotEmpty() && !busy) {
                            input = ""; messages.add(ChatMessage("user", q)); saveHistory(prefs, messages); busy = true; status = ""
                            scope.launch {
                                val answer = withContext(Dispatchers.IO) { callAi(endpoint, model, SecretVault.decrypt(context, prefs.getString("api_key_enc", null)), q, memoryText) }
                                messages.add(ChatMessage("assistant", answer)); saveHistory(prefs, messages); busy = false
                                if (answer.startsWith("त्रुटि:")) status = answer
                            }
                        }
                    }, onSpeak, status)
                    "Settings" -> SettingsPage(endpoint, { endpoint = it }, model, { model = it }, apiKey, { apiKey = it },
                        onSave = {
                            prefs.edit().putString("endpoint", endpoint.trim()).putString("model", model.trim())
                                .putString("api_key_enc", SecretVault.encrypt(context, apiKey.trim())).apply()
                            apiKey = ""; status = "सेटिंग्स सेव हो गईं।"
                        }, status = status)
                    "Yaadein" -> MemoriesPage(memoryText, { memoryText = it }, { prefs.edit().putString("memories", memoryText).apply(); status = "यादें सेव हो गईं।" }, status)
                    "Scan" -> ScanPage(onLaunchCamera = {
                        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                        if (intent.resolveActivity(context.packageManager) != null) {
                            runCatching { context.startActivity(intent) }.onFailure { status = "Camera नहीं खुल पाया।" }
                        } else status = "इस डिवाइस पर camera app उपलब्ध नहीं है।"
                    }, status)
                    else -> SimplePage(page, status) { page = "Home" }
                }
                }
                BottomBar(page, onNavigate = { page = it }, onMic = { micPermission.launch(Manifest.permission.RECORD_AUDIO) })
            }
        }
    }
}

private fun saveHistory(prefs: android.content.SharedPreferences, messages: List<ChatMessage>) {
    val arr = JSONArray()
    messages.takeLast(100).forEach { arr.put(JSONObject().put("role", it.role).put("text", it.text)) }
    prefs.edit().putString("history", arr.toString()).apply()
}

private fun callAi(endpoint: String, model: String, key: String?, question: String, memories: String): String {
    if (endpoint.isBlank() || model.isBlank() || key.isNullOrBlank()) {
        return "त्रुटि: AI चालू करने के लिए Settings में API endpoint, model और अपनी API key भरें।"
    }
    var connection: HttpURLConnection? = null
    return try {
        connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; connectTimeout = 15000; readTimeout = 45000; doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $key")
        }
        val system = "You are Pratiksha AI, a helpful personal assistant. Reply naturally in Hindi or English matching the user's language. User-controlled memories (use only as context): $memories"
        val body = JSONObject().put("model", model).put("messages", JSONArray()
            .put(JSONObject().put("role", "system").put("content", system))
            .put(JSONObject().put("role", "user").put("content", question)))
            .put("temperature", 0.7)
        connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val raw = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (code !in 200..299) {
            val detail = runCatching { JSONObject(raw).optJSONObject("error")?.optString("message") }.getOrNull()
            "त्रुटि: AI server ने HTTP $code दिया। ${detail ?: raw.take(180)}"
        } else {
            val json = JSONObject(raw)
            json.optJSONArray("choices")?.optJSONObject(0)?.optJSONObject("message")?.optString("content")
                ?.takeIf { it.isNotBlank() } ?: "त्रुटि: AI provider से जवाब का format समझ नहीं आया।"
        }
    } catch (e: java.net.SocketTimeoutException) {
        "त्रुटि: जवाब आने में समय लग गया। इंटरनेट जाँचकर फिर कोशिश करें।"
    } catch (e: java.net.UnknownHostException) {
        "त्रुटि: server नहीं मिला। Internet और API endpoint जाँचें।"
    } catch (e: Exception) {
        "त्रुटि: ${e.localizedMessage ?: "AI request पूरी नहीं हुई"}"
    } finally { connection?.disconnect() }
}

private object SecretVault {
    private const val alias = "PratikshaAIApiKey"
    private fun secretKey(): SecretKey {
        val ks = java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(alias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        return generator.generateKey()
    }
    fun encrypt(context: android.content.Context, value: String): String {
        if (value.isBlank()) return ""
        return runCatching {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE, secretKey())
            val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(encrypted, Base64.NO_WRAP)
        }.getOrDefault("")
    }
    fun decrypt(context: android.content.Context, value: String?): String? {
        if (value.isNullOrBlank()) return null
        return runCatching {
            val parts = value.split(":"); val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), javax.crypto.spec.GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)))
            String(cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)), Charsets.UTF_8)
        }.getOrNull()
    }
}

@Composable
private fun TopBar(onMenu: () -> Unit, onBell: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onMenu) { Icon(Icons.Default.Menu, null, tint = White) }
        Text("Pratiksha", Modifier.weight(1f), color = White, fontSize = 23.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
        IconButton(onClick = onBell) { Icon(Icons.Default.NotificationsNone, null, tint = White) }
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(Violet, Color(0xFF21133D)))), contentAlignment = Alignment.Center) {
            Text("P", color = White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
    }
}

@Composable
private fun HomePage(energy: Int, mood: String, onEnergy: () -> Unit, onPage: (String) -> Unit,
                     status: String, input: String, onInput: (String) -> Unit, onSend: () -> Unit, onMic: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text("Good morning,", color = Muted, fontSize = 20.sp)
                Text("Suchit Bhai ✨", color = White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(12.dp),
                modifier = Modifier.clickable { onEnergy() }) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚡  $energy", color = White, fontSize = 18.sp)
                    Text("Energy", color = Muted, fontSize = 12.sp)
                }
            }
        }
        Box(Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp), contentAlignment = Alignment.Center) {
            WireGlobe(Modifier.fillMaxWidth().heightIn(min = 220.dp, max = 340.dp))
            Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("P.R.A.T.I.K.S.H.A", color = Lavender, fontSize = 21.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                Text("BOLO, KAISE HELP KARUN?", color = Muted, fontSize = 12.sp, letterSpacing = 2.sp)
            }
        }
        Row(
horizontalArrangement = Arrangement.spacedBy(8.dp),
modifier = Modifier.fillMaxWidth()
) {
Shortcut("♫", "Gaane", Modifier.weight(1f)) {
onPage("Gaane")
}
Shortcut("▤", "Padhai", Modifier.weight(1f)) {
onPage("Padhai")
}
Shortcut("✎", "Journal", Modifier.weight(1f)) {
onPage("Journal")
}
        }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            InfoCard("☼ Mausam", "Live data connect करें", Modifier.weight(1f))
            InfoCard("▣ Aaj", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH)), Modifier.weight(1f))
            Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Panel).clickable { onPage("Mood") }.padding(12.dp)) {
                Text("♥ Mood", color = Muted, fontSize = 12.sp)
                Text(mood, color = White, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 2)
                Text("चुनने के लिए टैप करें", color = Muted, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Panel).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AttachFile, null, tint = Muted)
            BasicTextField(value = input, onValueChange = onInput, modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                textStyle = androidx.compose.ui.text.TextStyle(color = White, fontSize = 14.sp),
                decorationBox = { inner -> if (input.isEmpty()) Text("Pratiksha se kuch bhi poochho...", color = Muted, fontSize = 14.sp); inner() })
            IconButton(onClick = onSend) { Icon(Icons.Default.Send, "Send", tint = Lavender) }
        }
        if (status.isNotBlank()) Text(status, color = Lavender, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun WireGlobe(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "globe")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier) {
        val r = size.minDimension * 0.34f
        val c = Offset(
            size.width / 2,
            size.height / 2 - 12.dp.toPx()
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x553D2A75),
                    Color(0x113D2A75),
                    Color.Transparent
                ),
                center = c,
                radius = r * 1.8f
            ),
            radius = r * 1.8f,
            center = c
        )

        drawCircle(
            color = Color(0x665C3C91),
            radius = r,
            center = c,
            style = Stroke(1.2.dp.toPx())
        )

        for (i in -3..3) {
            val y = i * r / 4f
            val w = kotlin.math.sqrt(
                (r * r - y * y).coerceAtLeast(0f)
            )

            drawOval(
                color = Color(0xFF9C73E7).copy(alpha = 0.68f),
                topLeft = Offset(
                    c.x - w,
                    c.y + y - r * 0.12f
                ),
                size = androidx.compose.ui.geometry.Size(
                    w * 2,
                    r * 0.24f
                ),
                style = Stroke(1.dp.toPx())
            )
        }

        for (i in 0..8) {
            val angle = Math.toRadians(
                (i * 20.0 + rotation).toDouble()
            )

            val rx = kotlin.math.abs(
                kotlin.math.cos(angle)
            ).toFloat() * r

            val ry = kotlin.math.abs(
                kotlin.math.sin(angle)
            ).toFloat() * r

            drawOval(
                color = Color(0xFF9C73E7).copy(alpha = 0.35f),
                topLeft = Offset(
                    c.x - rx,
                    c.y - ry
                ),
                size = androidx.compose.ui.geometry.Size(
                    rx * 2,
                    ry * 2
                ),
                style = Stroke(1.dp.toPx())
            )
        }
    }
}  for (i in -3..3) {
            val y = i * r / 4f
            val w = kotlin.math.sqrt((r*r-y*y).coerceAtLeast(0f))
            drawOval(Color(0xFF9C73E7).copy(alpha = 0.68f), Offset(c.x-w, c.y+y-r*0.12f), androidx.compose.ui.geometry.Size(w*2, r*0.24f), style = Stroke(1.dp.toPx()))
        }
        for (i in 0..8) {
            val angle = Math.toRadians((i * 20.0 + rotation).toDouble())
            val rx = kotlin.math.abs(kotlin.math.cos(angle)).toFloat() * r
            drawLine(Color(0xFFAD83FF).copy(alpha = 0.65f), Offset(c.x-rx, c.y-r*0.9f), Offset(c.x+rx, c.y+r*0.9f), strokeWidth = 1.2.dp.toPx(), cap = StrokeCap.Round)
        }
        drawCircle(Color(0xFFDFC9FF), 3.dp.toPx(), Offset(c.x + kotlin.math.cos(Math.toRadians(rotation.toDouble())).toFloat()*r*0.8f, c.y+r*0.65f))
        drawCircle(Color(0xFFB994FF).copy(alpha=.5f), r*1.32f, c, style=Stroke(1.dp.toPx()))
    }
}

@Composable
private fun Shortcut(
symbol: String,
label: String,
modifier: Modifier = Modifier,
onClick: () -> Unit
) {
Row(
modifier
.clip(RoundedCornerShape(10.dp))
.background(Panel)
.clickable(onClick = onClick)
.padding(vertical = 14.dp),
horizontalArrangement = Arrangement.Center,
verticalAlignment = Alignment.CenterVertically
) {
Text(symbol, color = Lavender, fontSize = 18.sp)
Spacer(Modifier.width(6.dp))
Text(label, color = White, fontSize = 13.sp)
}
}
@Composable
private fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(Panel).padding(11.dp)) {
        Text(title, color = Muted, fontSize = 12.sp); Spacer(Modifier.height(10.dp))
        Text(value, color = White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
@Composable
private fun BottomBar(page: String, onNavigate: (String) -> Unit, onMic: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(Color(0xFF100E20)).padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
        NavItem("Home", Icons.Default.Home, page == "Home") { onNavigate("Home") }
        NavItem("Scan", Icons.Default.CenterFocusStrong, page == "Scan") { onNavigate("Scan") }
        Box(Modifier.size(62.dp).clip(CircleShape).background(Violet).clickable(onClick = onMic), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Mic, "बोलकर पूछें", tint = White, modifier = Modifier.size(30.dp))
        }
        NavItem("Yaadein", Icons.Default.Psychology, page == "Yaadein") { onNavigate("Yaadein") }
        NavItem("Chat", Icons.Default.ChatBubbleOutline, page == "Chat") { onNavigate("Chat") }
    }
}
@Composable
private fun NavItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick).padding(horizontal = 6.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = if (selected) Violet else Muted, modifier = Modifier.size(22.dp))
        Text(label, color = if (selected) Violet else Muted, fontSize = 10.sp)
    }
}
@Composable
private fun ChatPage(messages: List<ChatMessage>, busy: Boolean, input: String, onInput: (String) -> Unit,
                     onSend: () -> Unit, onSpeak: (String) -> Unit, status: String) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text("Chat with Pratiksha", color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), reverseLayout = false) {
            items(messages) { msg ->
                Column(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalAlignment = if (msg.role == "user") Alignment.End else Alignment.Start) {
                    Text(if (msg.role == "user") "आप" else "प्रतिक्षा", color = Lavender, fontSize = 11.sp)
                    Text(msg.text, color = White, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(if (msg.role == "user") Color(0xFF34245A) else Panel).padding(12.dp))
                    if (msg.role == "assistant" && !msg.text.startsWith("त्रुटि:")) Text("🔊 जवाब सुनें", color = Muted, modifier = Modifier.clickable { onSpeak(msg.text) }.padding(4.dp), fontSize = 12.sp)
                }
            }
        }
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Violet)
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp).clip(RoundedCornerShape(14.dp)).background(Panel).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(input, onInput, Modifier.weight(1f).padding(8.dp), textStyle = androidx.compose.ui.text.TextStyle(color = White),
                decorationBox = { inner -> if (input.isEmpty()) Text("अपना सवाल लिखें…", color = Muted); inner() })
            IconButton(onClick = onSend) { Icon(Icons.Default.Send, null, tint = Lavender) }
        }
        if (status.isNotBlank()) Text(status, color = Lavender, fontSize = 12.sp)
    }
}
@Composable
private fun SettingsPage(endpoint: String, onEndpoint: (String) -> Unit, model: String, onModel: (String) -> Unit,
                         apiKey: String, onApiKey: (String) -> Unit, onSave: () -> Unit, status: String) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("AI Settings", color = White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("OpenAI-compatible Chat Completions endpoint इस्तेमाल करें। अपनी key केवल अपने भरोसेमंद provider की दें।", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 10.dp))
        SettingField("API endpoint", endpoint, onEndpoint, false)
        SettingField("Model name", model, onModel, false)
        SettingField("API key (दोबारा दिखेगी नहीं)", apiKey, onApiKey, true)
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Violet)) { Text("सेटिंग्स सुरक्षित करें") }
        Text("API key Android Keystore से encrypt करके इस फोन पर रखी जाती है। ऐप में कोई key hardcode नहीं है। Public APK में personal key डालकर share न करें।", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
        if (status.isNotBlank()) Text(status, color = Lavender, fontSize = 12.sp)
    }
}
@Composable
private fun SettingField(label: String, value: String, onValue: (String) -> Unit, secret: Boolean) {
    Column(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
        Text(label, color = Lavender, fontSize = 13.sp)
        OutlinedTextField(value, onValue, Modifier.fillMaxWidth(), singleLine = true,
            visualTransformation = if (secret) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = White, unfocusedTextColor = White, focusedBorderColor = Violet, unfocusedBorderColor = Muted))
    }
}
@Composable
private fun MemoriesPage(value: String, onChange: (String) -> Unit, onSave: () -> Unit, status: String) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Yaadein / Memories", color = White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("यहाँ केवल वही बातें लिखें जिन्हें आप चाहते हैं कि assistant आगे के जवाबों में ध्यान रखे। इन्हें कभी भी बदल या मिटा सकते हैं।", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 12.dp))
        OutlinedTextField(value, onChange, Modifier.fillMaxWidth().weight(1f), placeholder = { Text("उदाहरण: मुझे हिंदी में जवाब पसंद हैं…") },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = White, unfocusedTextColor = White, focusedBorderColor = Violet))
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Violet)) { Text("यादें सेव करें") }
        if (status.isNotBlank()) Text(status, color = Lavender, fontSize = 12.sp)
    }
}
@Composable
private fun ScanPage(onLaunchCamera: () -> Unit, status: String) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.CameraAlt, null, tint = Lavender, modifier = Modifier.size(64.dp))
        Text("Scan", color = White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("फोटो लेने के लिए आपके फोन का camera app खुलेगा। इस ऐप में camera permission नहीं माँगी जाती।", color = Muted, textAlign = TextAlign.Center, modifier = Modifier.padding(14.dp))
        Button(onClick = onLaunchCamera, colors = ButtonDefaults.buttonColors(containerColor = Violet)) { Text("Camera खोलें") }
        if (status.isNotBlank()) Text(status, color = Lavender)
    }
}
@Composable
private fun SimplePage(title: String, status: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(title, color = White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(when(title) {
            "Mood" -> "आज आपका मूड कैसा है?"
            "Gaane" -> "अपने AI provider से गानों की जानकारी पूछने के लिए Chat खोलें।"
            "Padhai" -> "पढ़ाई में मदद के लिए Chat में अपना सवाल पूछें।"
            "Journal" -> "Journal लिखने की सुविधा अगले चरण में जोड़ी जा सकती है।"
            else -> "यह सुविधा अभी configure नहीं है।"
        }, color = Muted, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
        if (title == "Mood") listOf("😊 अच्छा", "😐 ठीक-ठाक", "😔 उदास", "🥰 प्यार भरी").forEach { mood ->
            Text(mood, color = White, modifier = Modifier.fillMaxWidth().padding(4.dp).clip(RoundedCornerShape(12.dp)).background(Panel).clickable { /* selection is saved from home on next iteration */ }.padding(14.dp), textAlign = TextAlign.Center)
        }
        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Violet)) { Text("Home पर जाएँ") }
        if (status.isNotBlank()) Text(status, color = Lavender, fontSize = 12.sp)
    }
}
package in.pratiksha.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64

private val Night = Color(0xFF090916)
private val Panel = Color(0xFF17152B)
private val Violet = Color(0xFF955BFF)
private val Lavender = Color(0xFFC9A8FF)
private val Muted = Color(0xFF9990B8)
private val White = Color(0xFFF8F5FF)

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
    private fun speak(text: String) { tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "pratiksha-reply") }
    override fun onDestroy() { tts?.shutdown(); super.onDestroy() }
}

private data class ChatMessage(val role: String, val text: String)

@Composable
private fun PratikshaApp(onSpeak: (String) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pratiksha", Activity.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    var page by remember { mutableStateOf("Home") }
    var input by remember { mutableStateOf("") }
    var endpoint by remember { mutableStateOf(prefs.getString("endpoint", "https://api.openai.com/v1/chat/completions") ?: "") }
    var model by remember { mutableStateOf(prefs.getString("model", "gpt-4o-mini") ?: "") }
    var apiKey by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var energy by remember { mutableIntStateOf(prefs.getInt("energy", 2)) }
    var mood by remember { mutableStateOf(prefs.getString("mood", "अपना मूड चुनें") ?: "अपना मूड चुनें") }
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

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) { input = spoken; page = "Chat" }
        }
    }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "प्रतिक्षा से बोलिए")
            }
            runCatching { speechLauncher.launch(intent) }.onFailure { status = "इस फोन पर speech recognition उपलब्ध नहीं है।" }
        } else status = "Voice input के लिए microphone permission चाहिए।"
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Violet, secondary = Lavender, background = Night,
        surface = Panel, onBackground = White, onSurface = White
    )) {
        Surface(Modifier.fillMaxSize(), color = Night) {
            Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0B0A18), Night, Color(0xFF121027))))) {
                TopBar(onMenu = { page = if (page == "Settings") "Home" else "Settings" }, onBell = { status = "अभी कोई notification नहीं है।" })
                Box(Modifier.weight(1f).fillMaxWidth()) {
                when (page) {
                    "Home" -> HomePage(energy, mood, onEnergy = { energy = (energy + 1) % 10; prefs.edit().putInt("energy", energy).apply() },
                        onPage = { page = it }, status = status, input = input, onInput = { input = it },
                        onSend = {
                            val question = input.trim()
                            if (question.isNotEmpty() && !busy) {
                                input = ""; messages.add(ChatMessage("user", question)); saveHistory(prefs, messages)
                                page = "Chat"; busy = true; status = ""
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        callAi(endpoint, model, SecretVault.decrypt(context, prefs.getString("api_key_enc", null)), question, memoryText)
                                    }
                                    messages.add(ChatMessage("assistant", result))
                                    saveHistory(prefs, messages)
                                    busy = false
                                    if (result.startsWith("त्रुटि:")) status = result
                                }
                            }
                        }, onMic = { micPermission.launch(Manifest.permission.RECORD_AUDIO) })
                    "Chat" -> ChatPage(messages, busy, input, { input = it }, {
                        val q = input.trim()
                        if (q.isNotEmpty() && !busy) {
                            input = ""; messages.add(ChatMessage("user", q)); saveHistory(prefs, messages); busy = true; status = ""
                            scope.launch {
                                val answer = withContext(Dispatchers.IO) { callAi(endpoint, model, SecretVault.decrypt(context, prefs.getString("api_key_enc", null)), q, memoryText) }
                                messages.add(ChatMessage("assistant", answer)); saveHistory(prefs, messages); busy = false
                                if (answer.startsWith("त्रुटि:")) status = answer
                            }
                        }
                    }, onSpeak, status)
                    "Settings" -> SettingsPage(endpoint, { endpoint = it }, model, { model = it }, apiKey, { apiKey = it },
                        onSave = {
                            prefs.edit().putString("endpoint", endpoint.trim()).putString("model", model.trim())
                                .putString("api_key_enc", SecretVault.encrypt(context, apiKey.trim())).apply()
                            apiKey = ""; status = "सेटिंग्स सेव हो गईं।"
                        }, status = status)
                    "Yaadein" -> MemoriesPage(memoryText, { memoryText = it }, { prefs.edit().putString("memories", memoryText).apply(); status = "यादें सेव हो गईं।" }, status)
                    "Scan" -> ScanPage(onLaunchCamera = {
                        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                        if (intent.resolveActivity(context.packageManager) != null) {
                            runCatching { context.startActivity(intent) }.onFailure { status = "Camera नहीं खुल पाया।" }
                        } else status = "इस डिवाइस पर camera app उपलब्ध नहीं है।"
                    }, status)
                    else -> SimplePage(page, status) { page = "Home" }
                }
                }
                BottomBar(page, onNavigate = { page = it }, onMic = { micPermission.launch(Manifest.permission.RECORD_AUDIO) })
            }
        }
    }
}

private fun saveHistory(prefs: android.content.SharedPreferences, messages: List<ChatMessage>) {
    val arr = JSONArray()
    messages.takeLast(100).forEach { arr.put(JSONObject().put("role", it.role).put("text", it.text)) }
    prefs.edit().putString("history", arr.toString()).apply()
}

private fun callAi(endpoint: String, model: String, key: String?, question: String, memories: String): String {
    if (endpoint.isBlank() || model.isBlank() || key.isNullOrBlank()) {
        return "त्रुटि: AI चालू करने के लिए Settings में API endpoint, model और अपनी API key भरें।"
    }
    var connection: HttpURLConnection? = null
    return try {
        connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; connectTimeout = 15000; readTimeout = 45000; doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $key")
        }
        val system = "You are Pratiksha AI, a helpful personal assistant. Reply naturally in Hindi or English matching the user's language. User-controlled memories (use only as context): $memories"
        val body = JSONObject().put("model", model).put("messages", JSONArray()
            .put(JSONObject().put("role", "system").put("content", system))
            .put(JSONObject().put("role", "user").put("content", question)))
            .put("temperature", 0.7)
        connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val raw = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (code !in 200..299) {
            val detail = runCatching { JSONObject(raw).optJSONObject("error")?.optString("message") }.getOrNull()
            "त्रुटि: AI server ने HTTP $code दिया। ${detail ?: raw.take(180)}"
        } else {
            val json = JSONObject(raw)
            json.optJSONArray("choices")?.optJSONObject(0)?.optJSONObject("message")?.optString("content")
                ?.takeIf { it.isNotBlank() } ?: "त्रुटि: AI provider से जवाब का format समझ नहीं आया।"
        }
    } catch (e: java.net.SocketTimeoutException) {
        "त्रुटि: जवाब आने में समय लग गया। इंटरनेट जाँचकर फिर कोशिश करें।"
    } catch (e: java.net.UnknownHostException) {
        "त्रुटि: server नहीं मिला। Internet और API endpoint जाँचें।"
    } catch (e: Exception) {
        "त्रुटि: ${e.localizedMessage ?: "AI request पूरी नहीं हुई"}"
    } finally { connection?.disconnect() }
}

private object SecretVault {
    private const val alias = "PratikshaAIApiKey"
    private fun secretKey(): SecretKey {
        val ks = java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(alias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        return generator.generateKey()
    }
    fun encrypt(context: android.content.Context, value: String): String {
        if (value.isBlank()) return ""
        return runCatching {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE, secretKey())
            val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(encrypted, Base64.NO_WRAP)
        }.getOrDefault("")
    }
    fun decrypt(context: android.content.Context, value: String?): String? {
        if (value.isNullOrBlank()) return null
        return runCatching {
            val parts = value.split(":"); val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), javax.crypto.spec.GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)))
            String(cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)), Charsets.UTF_8)
        }.getOrNull()
    }
}

@Composable
private fun TopBar(onMenu: () -> Unit, onBell: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onMenu) { Icon(Icons.Default.Menu, null, tint = White) }
        Text("Pratiksha", Modifier.weight(1f), color = White, fontSize = 23.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
        IconButton(onClick = onBell) { Icon(Icons.Default.NotificationsNone, null, tint = White) }
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(Violet, Color(0xFF21133D)))), contentAlignment = Alignment.Center) {
            Text("P", color = White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
    }
}

@Composable
private fun HomePage(energy: Int, mood: String, onEnergy: () -> Unit, onPage: (String) -> Unit,
                     status: String, input: String, onInput: (String) -> Unit, onSend: () -> Unit, onMic: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text("Good morning,", color = Muted, fontSize = 20.sp)
                Text("Suchit Bhai ✨", color = White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(12.dp),
                modifier = Modifier.clickable { onEnergy() }) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚡  $energy", color = White, fontSize = 18.sp)
                    Text("Energy", color = Muted, fontSize = 12.sp)
                }
            }
        }
        Box(Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp), contentAlignment = Alignment.Center) {
            WireGlobe(Modifier.fillMaxWidth().heightIn(min = 220.dp, max = 340.dp))
            Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("P.R.A.T.I.K.S.H.A", color = Lavender, fontSize = 21.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                Text("BOLO, KAISE HELP KARUN?", color = Muted, fontSize = 12.sp, letterSpacing = 2.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Shortcut("♫", "Gaane") { onPage("Gaane") }
            Shortcut("▤", "Padhai") { onPage("Padhai") }
            Shortcut("✎", "Journal") { onPage("Journal") }
        }Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            InfoCard("☼ Mausam", "Live data connect करें", Modifier.weight(1f))
            InfoCard("▣ Aaj", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH)), Modifier.weight(1f))
            Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Panel).clickable { onPage("Mood") }.padding(12.dp)) {
                Text("♥ Mood", color = Muted, fontSize = 12.sp)
                Text(mood, color = White, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 2)
                Text("चुनने के लिए टैप करें", color = Muted, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Panel).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AttachFile, null, tint = Muted)
            BasicTextField(value = input, onValueChange = onInput, modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                textStyle = androidx.compose.ui.text.TextStyle(color = White, fontSize = 14.sp),
                decorationBox = { inner -> if (input.isEmpty()) Text("Pratiksha se kuch bhi poochho...", color = Muted, fontSize = 14.sp); inner() })
            IconButton(onClick = onSend) { Icon(Icons.Default.Send, "Send", tint = Lavender) }
        }
        if (status.isNotBlank()) Text(status, color = Lavender, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun WireGlobe(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "globe")
    val rotation by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart), label = "rotation")
    Canvas(modifier) {
        val r = size.minDimension * 0.34f
        val c = Offset(size.width / 2, size.height / 2 - 12.dp.toPx())
        drawCircle(Brush.radialGradient(listOf(Color(0x553D2A75), Color(0x113D2A75), Color.Transparent), c, r * 1.8f), r * 1.8f)
        drawCircle(Color(0x665C3C91), r, c, style = Stroke(1.2.dp.toPx()))
        for (i in -3..3) {
            val y = i * r / 4f
            val w = kotlin.math.sqrt((r*r-y*y).coerceAtLeast(0f))
            drawOval(Color(0xFF9C73E7).copy(alpha = 0.68f), Offset(c.x-w, c.y+y-r*0.12f), androidx.compose.ui.geometry.Size(w*2, r*0.24f), style = Stroke(1.dp.toPx()))
        }
        for (i in 0..8) {
            val angle = Math.toRadians((i * 20.0 + rotation).toDouble())
            val rx = kotlin.math.abs(kotlin.math.cos(angle)).toFloat() * r
            val ry = kotlin.math.abs(kotlin.math.sin(angle)).toFloat() * r

            drawOval(
                color = Color(0xFF9C73E7).copy(alpha = 0.35f),
                topLeft = Offset(c.x - rx, c.y - ry),
                size = androidx.compose.ui.geometry.Size(rx * 2, ry * 2),
                style = Stroke(1.dp.toPx())
            )
        }
