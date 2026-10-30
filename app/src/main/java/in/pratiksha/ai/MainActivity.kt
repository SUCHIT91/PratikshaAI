package `in`.pratiksha.ai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

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


@Composable
private fun PratikshaApp(onSpeak: (String) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pratiksha", Activity.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    var page by remember { mutableStateOf("Home") }
    var profileName by remember { mutableStateOf(prefs.getString("profile_name", "") ?: "") }
    var profileEmail by remember { mutableStateOf(prefs.getString("profile_email", "") ?: "") }
    var profilePhone by remember { mutableStateOf(prefs.getString("profile_phone", "") ?: "") }
    var previousPage by remember { mutableStateOf("Home") }
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
    val agents = remember {
        mutableStateListOf<String>().apply {
            runCatching {
                val arr = JSONArray(prefs.getString("sub_agents", "[]") ?: "[]")
                for (i in 0 until arr.length()) add(arr.optString(i))
            }
        }
    }
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

    // Ek hi jagah se message bhejne ka logic
    val sendQuestion: (String) -> Unit = { rawQuestion ->
        val question = rawQuestion.trim()
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
                        memoryText,
                        messages.dropLast(1).takeLast(12)
                    )
                }
                messages.add(ChatMessage("assistant", result))
                saveHistory(prefs, messages)
                busy = false
                if (result.startsWith("त्रुटि:")) {
                    status = result
                } else {
                    onSpeak(result)
                }
            }
        }
    }

    val sendMessage: () -> Unit = {
        sendQuestion(input)
    }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                input = spoken
                page = "Chat"
                sendQuestion(spoken)
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
                    .background(Brush.verticalGradient(listOf(Color(0xFF080D1C), Color(0xFF111A30), Color(0xFF080D1C))))
            ) {
                TopBar(
                    onMenu = { page = if (page == "Menu") "Home" else "Menu" },
                    onBell = { status = "अभी कोई notification नहीं है।" },
                    onProfile = {
                        previousPage = page
                        page = "Profile"
                        status = ""
                    }
                )
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (page) {
                        "Menu" -> DrawerMenu(onNavigate = { page = it })
                        "Sub-agents" -> SubAgentsPage(
                            agents = agents,
                            onAdd = { name, instructions ->
                                agents.add("$name::$instructions")
                                val arr = JSONArray()
                                agents.forEach { arr.put(it) }
                                prefs.edit().putString("sub_agents", arr.toString()).apply()
                                status = "Agent add ho gaya. Use chalane ke liye model/API connection zaroori hai."
                            },
                            onRemove = { index ->
                                if (index in agents.indices) {
                                    agents.removeAt(index)
                                    val arr = JSONArray()
                                    agents.forEach { arr.put(it) }
                                    prefs.edit().putString("sub_agents", arr.toString()).apply()
                                    status = "Agent remove ho gaya."
                                }
                            },
                            status = status
                        )

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
                            onMic = { micPermission.launch(Manifest.permission.RECORD_AUDIO) },
                            status = status
                        )

                        "Profile" -> ProfilePage(
                            name = profileName,
                            onName = { profileName = it },
                            email = profileEmail,
                            onEmail = { profileEmail = it },
                            phone = profilePhone,
                            onPhone = { profilePhone = it },
                            onSave = {
                                prefs.edit()
                                    .putString("profile_name", profileName.trim())
                                    .putString("profile_email", profileEmail.trim())
                                    .putString("profile_phone", profilePhone.trim())
                                    .apply()
                                status = "Profile सेव हो गई।"
                                page = "Home"
                            },
                            onCancel = {
                                profileName = prefs.getString("profile_name", "") ?: ""
                                profileEmail = prefs.getString("profile_email", "") ?: ""
                                profilePhone = prefs.getString("profile_phone", "") ?: ""
                                page = previousPage
                                status = ""
                            },
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
