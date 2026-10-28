package `in`.pratiksha.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ChatPage(
    messages: List<ChatMessage>,
    busy: Boolean,
    input: String,
    onInput: (String) -> Unit,
    onSend: () -> Unit,
    onSpeak: (String) -> Unit,
    status: String
) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val mine = msg.role == "user"
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (mine) PkViolet else PkPanel)
                            .padding(12.dp)
                            .widthIn(max = 300.dp)
                    ) {
                        Text(msg.text, color = PkWhite, fontSize = 14.sp)
                        if (!mine) {
                            Text(
                                "🔊 सुनें",
                                color = PkLavender,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 6.dp).clickable { onSpeak(msg.text) }
                            )
                        }
                    }
                }
            }
            if (busy) {
                item { Text("प्रतिक्षा सोच रही है...", color = PkMuted, fontSize = 13.sp) }
            }
        }
        if (status.isNotBlank()) {
            Text(status, color = PkLavender, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
        }
        Spacer(Modifier.height(8.dp))
        InputBar(input = input, onInput = onInput, onSend = onSend, onMic = null, hint = "यहाँ लिखें...")
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
internal fun LabeledField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    hidden: Boolean = false
) {
    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(label, color = PkMuted, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PkPanel)
                .padding(14.dp),
            textStyle = TextStyle(color = PkWhite, fontSize = 14.sp),
            cursorBrush = SolidColor(PkLavender),
            visualTransformation = if (hidden) PasswordVisualTransformation() else VisualTransformation.None
        )
    }
}

@Composable
internal fun SettingsPage(
    endpoint: String,
    onEndpoint: (String) -> Unit,
    model: String,
    onModel: (String) -> Unit,
    apiKey: String,
    onApiKey: (String) -> Unit,
    onSave: () -> Unit,
    status: String
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Text("Settings", color = PkWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        LabeledField("API endpoint", endpoint, onEndpoint)
        LabeledField("Model", model, onModel)
        LabeledField("API key (फोन में encrypted सेव होती है)", apiKey, onApiKey, hidden = true)
        Button(onClick = onSave, colors = ButtonDefaults.buttonColors(containerColor = PkViolet)) {
            Text("सेव करें", color = PkWhite)
        }
        if (status.isNotBlank()) {
            Text(status, color = PkLavender, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
internal fun MemoriesPage(text: String, onText: (String) -> Unit, onSave: () -> Unit, status: String) {
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Text("Yaadein", color = PkWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("जो बातें Pratiksha को याद रखनी हैं, यहाँ लिखें।", color = PkMuted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        BasicTextField(
            value = text,
            onValueChange = onText,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PkPanel)
                .padding(14.dp),
            textStyle = TextStyle(color = PkWhite, fontSize = 14.sp),
            cursorBrush = SolidColor(PkLavender)
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onSave, colors = ButtonDefaults.buttonColors(containerColor = PkViolet)) {
            Text("सेव करें", color = PkWhite)
        }
        if (status.isNotBlank()) {
            Text(status, color = PkLavender, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
internal fun ScanPage(onLaunchCamera: () -> Unit, status: String) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Scan", color = PkWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Camera खोलकर फोटो लें।", color = PkMuted, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onLaunchCamera, colors = ButtonDefaults.buttonColors(containerColor = PkViolet)) {
            Text("📷 Camera खोलें", color = PkWhite)
        }
        if (status.isNotBlank()) {
            Text(status, color = PkLavender, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
internal fun SimplePage(title: String, status: String, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = PkWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("यह सेक्शन जल्द आ रहा है।", color = PkMuted, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = PkViolet)) {
            Text("Home पर वापस", color = PkWhite)
        }
        if (status.isNotBlank()) {
            Text(status, color = PkLavender, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
internal fun BottomBar(page: String, onNavigate: (String) -> Unit, onMic: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(PkPanel)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem("⌂", "Home", page == "Home", Modifier.weight(1f)) { onNavigate("Home") }
        NavItem("⌖", "Scan", page == "Scan", Modifier.weight(1f)) { onNavigate("Scan") }
        Box(
            Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(PkViolet, Color(0xFF5B2FD0))))
                .clickable { onMic() },
            contentAlignment = Alignment.Center
        ) {
            Text("🎤", fontSize = 22.sp)
        }
        NavItem("❖", "Yaadein", page == "Yaadein", Modifier.weight(1f)) { onNavigate("Yaadein") }
        NavItem("✉", "Chat", page == "Chat", Modifier.weight(1f)) { onNavigate("Chat") }
    }
}

@Composable
internal fun NavItem(icon: String, label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val tint = if (selected) PkLavender else PkMuted
    Column(
        modifier.clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, color = tint, fontSize = 20.sp)
        Text(label, color = tint, fontSize = 11.sp)
    }
}


@Composable
internal fun DrawerMenu(onNavigate: (String) -> Unit) {
    val groups = listOf(
        "HOME" to listOf("Home", "Pratiksha Home"),
        "KAAM-KAAJ" to listOf("Markets", "Marketing", "Documents", "Meri photos", "Website / Coding", "Browser", "Accessibility"),
        "ASSISTANT" to listOf("Sub-agents", "Skills", "Settings", "Yaadein")
    )
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 18.dp)) {
                Box(
                    Modifier.size(52.dp).clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(PkLavender, PkViolet))),
                    contentAlignment = Alignment.Center
                ) { Text("P", color = PkWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold) }
                Column(Modifier.padding(start = 14.dp)) {
                    Text("Pratiksha AI", color = PkWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Personal AI Assistant", color = PkMuted, fontSize = 13.sp)
                }
            }
        }
        groups.forEach { (heading, entries) ->
            item { Text(heading, color = PkMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) }
            items(entries) { entry ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(if (entry == "Sub-agents") Color(0x332F67FF) else PkPanel)
                        .clickable { onNavigate(entry) }
                        .padding(horizontal = 18.dp, vertical = 17.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        when (entry) {
                            "Home" -> "⌂"
                            "Sub-agents" -> "✦"
                            "Settings" -> "⚙"
                            "Browser" -> "◎"
                            "Yaadein" -> "▣"
                            else -> "›"
                        },
                        color = PkLavender, fontSize = 20.sp
                    )
                    Text(entry, color = PkWhite, fontSize = 16.sp, modifier = Modifier.padding(start = 14.dp))
                }
            }
        }
        item {
            Text("Pratiksha AI • Pink Neon Edition", color = PkMuted, fontSize = 11.sp, modifier = Modifier.padding(vertical = 20.dp))
        }
    }
}

@Composable
internal fun SubAgentsPage(
    agents: List<String>,
    onAdd: (String, String) -> Unit,
    onRemove: (Int) -> Unit,
    status: String
) {
    var agentName by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var instructions by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Text("Sub-agents", color = PkWhite, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Apne kaam ke liye alag AI helpers banao.", color = PkMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (agents.isEmpty()) {
                item {
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(PkPanel).padding(18.dp)) {
                        Text("Abhi koi agent nahi hai", color = PkWhite, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("Neeche naam aur instructions bhar kar + Add Agent dabao.", color = PkMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
            items(agents.indices.toList()) { index ->
                val parts = agents[index].split("::", limit = 2)
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(PkPanel).padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✦", color = PkLavender, fontSize = 20.sp)
                        Text(parts.firstOrNull().orEmpty(), color = PkWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f).padding(start = 10.dp))
                        Text("Remove", color = PkLavender, modifier = Modifier.clickable { onRemove(index) }.padding(6.dp))
                    }
                    Text(parts.getOrElse(1) { "No instructions added" }, color = PkMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
        Text("Naya agent", color = PkWhite, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp, bottom = 8.dp))
        LabeledField("Agent ka naam", agentName, { agentName = it })
        LabeledField("Kaam / instructions", instructions, { instructions = it })
        Button(
            onClick = {
                if (agentName.isNotBlank()) {
                    onAdd(agentName.trim(), instructions.trim())
                    agentName = ""
                    instructions = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PkViolet)
        ) { Text("+ Add Agent", color = PkWhite) }
        if (status.isNotBlank()) Text(status, color = PkLavender, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
        Spacer(Modifier.height(8.dp))
    }
}
