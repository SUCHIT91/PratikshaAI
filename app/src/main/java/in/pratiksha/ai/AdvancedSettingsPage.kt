package `in`.pratiksha.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.IconButton

private val AdvBg = Color(0xFF080D1C)
private val AdvCard = Color(0xFF151F35)
private val AdvBlue = Color(0xFF65A9FF)
private val AdvText = Color(0xFFF3F6FF)
private val AdvMuted = Color(0xFF9AA9C4)

@Composable
internal fun AdvancedSettingsPage(
    settings: Map<String, Boolean>,
    onSettingChange: (String, Boolean) -> Unit,
    typingSpeed: Float,
    onTypingSpeedChange: (Float) -> Unit,
    onBack: () -> Unit,
    onOpenApiSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AdvBg)
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = AdvText)
                }
                Column(Modifier.weight(1f)) {
                    Text("Advanced", color = AdvText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    Text("Customize your Maya experience", color = AdvMuted, fontSize = 12.sp)
                }
                Icon(Icons.Default.Tune, "Advanced settings", tint = AdvBlue, modifier = Modifier.size(26.dp))
            }
        }

        item { SectionTitle("LOOK AUR FEEL") }
        item {
            SettingRow("🎨", "Theme", "Colours, font, text size aur corners") {
                onSettingChange("theme", !(settings["theme"] ?: false))
            }
        }
        item {
            SettingRow("🔮", "Appearance", "Orb style, colour aur size") {
                onSettingChange("appearance", !(settings["appearance"] ?: false))
            }
        }
        item {
            SettingRow("⚙️", "Behaviour", "Floating orb, echo guard, boot pe start") {
                onSettingChange("behaviour", !(settings["behaviour"] ?: false))
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AdvCard),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("⌨️  Typing", color = AdvText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("Typing speed: ${(typingSpeed * 100).toInt()}%", color = AdvMuted, fontSize = 12.sp)
                    Slider(
                        value = typingSpeed,
                        onValueChange = onTypingSpeedChange,
                        valueRange = 0.25f..2f
                    )
                    Text("Editors me insaan jaisi speed se typing", color = AdvMuted, fontSize = 12.sp)
                }
            }
        }

        item { SectionTitle("SAFETY AUR ACCESS") }
        item {
            SettingRow("🎙️", "Voice Guardian", "Sirf tumhari awaaz ka jawab de") {
                onSettingChange("voice_guardian", !(settings["voice_guardian"] ?: false))
            }
        }
        item {
            SettingRow("👤", "Chehre", "Tumhe aur tumhare logon ko pehchane") {
                onSettingChange("face_recognition", !(settings["face_recognition"] ?: false))
            }
        }
        item {
            SettingRow("🆘", "Emergency SOS", "Contacts, siren aur kya bheja jaata hai") {
                onSettingChange("emergency_sos", !(settings["emergency_sos"] ?: false))
            }
        }
        item {
            SettingRow("🛡️", "Touch Guard", "Tum door ho tab phone pe nazar rakhe") {
                onSettingChange("touch_guard", !(settings["touch_guard"] ?: false))
            }
        }
        item {
            SettingRow("🔒", "Screen lock", "Phone jagana aur secure unlock flow") {
                onSettingChange("screen_lock", !(settings["screen_lock"] ?: false))
            }
        }
        item {
            SettingRow("🔑", "Permissions", "Maya ke features ke liye zaroori access") {
                onSettingChange("permissions", !(settings["permissions"] ?: false))
            }
        }

        item { SectionTitle("SYSTEM") }
        item {
            SettingRow("⚡", "Event triggers", "Wo khud se kya batati hai") {
                onSettingChange("event_triggers", !(settings["event_triggers"] ?: false))
            }
        }
        item {
            SettingRow("🔌", "API Settings", "AI endpoint, model aur API key", onOpenApiSettings)
        }
        item {
            Text(
                "Note: Safety switches yahan abhi preferences save karte hain. Camera recognition, SOS actions, boot start aur secure device unlock ke liye alag Android implementation zaroori hai.",
                color = AdvMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(vertical = 14.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        color = AdvBlue,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
        modifier = Modifier.padding(top = 14.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingRow(
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AdvCard)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 23.sp)
            Column(Modifier.weight(1f).padding(start = 13.dp, end = 8.dp)) {
                Text(title, color = AdvText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Text(description, color = AdvMuted, fontSize = 11.sp, lineHeight = 15.sp)
            }
            Text("›", color = AdvBlue, fontSize = 26.sp)
        }
    }
}
