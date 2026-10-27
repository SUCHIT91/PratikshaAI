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
