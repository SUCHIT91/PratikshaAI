package `in`.pratiksha.ai

import androidx.compose.runtime.getValue
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt

// ---------- UI ----------

@Composable
internal fun TopBar(onMenu: () -> Unit, onBell: () -> Unit) {
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
internal fun HomePage(
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
                Text("नमस्ते!", color = PkMuted, fontSize = 20.sp)
                Text("मैं प्रतिक्षा AI हूँ ✨", color = PkWhite, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = PkPanel),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.clickable { onEnergy() }
            ) {
                Column(
                    Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚡  $energy", color = PkWhite, fontSize = 18.sp)
                    Text("Energy", color = PkMuted, fontSize = 12.sp)
                }
            }
        }

        Box(
            Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            WireGlobe(Modifier.fillMaxWidth().heightIn(min = 220.dp, max = 340.dp))
            Column(
                Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "P.R.A.T.I.K.S.H.A",
                    color = PkLavender,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
                Text("BOLO, KAISE HELP KARUN?", color = PkMuted, fontSize = 12.sp, letterSpacing = 2.sp)
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Shortcut("♫", "Gaane", Modifier.weight(1f)) { onPage("Gaane") }
            Shortcut("▤", "Padhai", Modifier.weight(1f)) { onPage("Padhai") }
            Shortcut("✎", "Journal", Modifier.weight(1f)) { onPage("Journal") }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            InfoCard("☼ Mausam", "Live data connect करें", Modifier.weight(1f))
            InfoCard(
                "▣ Aaj",
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH)),
                Modifier.weight(1f)
            )
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PkPanel)
                    .clickable { onPage("Mood") }
                    .padding(12.dp)
            ) {
                Text("♥ Mood", color = PkMuted, fontSize = 12.sp)
                Text(mood, color = PkWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 2)
                Text("चुनने के लिए टैप करें", color = PkMuted, fontSize = 10.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        InputBar(
            input = input,
            onInput = onInput,
            onSend = onSend,
            onMic = onMic,
            hint = "Pratiksha se kuch bhi poochho..."
        )

        if (status.isNotBlank()) {
            Text(status, color = PkLavender, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
internal fun InputBar(
    input: String,
    onInput: (String) -> Unit,
    onSend: () -> Unit,
    onMic: (() -> Unit)?,
    hint: String
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PkPanel)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("📎", fontSize = 18.sp)
        BasicTextField(
            value = input,
            onValueChange = onInput,
            modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
            textStyle = TextStyle(color = PkWhite, fontSize = 14.sp),
            cursorBrush = SolidColor(PkLavender),
            decorationBox = { inner ->
                Box {
                    if (input.isEmpty()) Text(hint, color = PkMuted, fontSize = 14.sp)
                    inner()
                }
            }
        )
        if (onMic != null) {
            IconButton(onClick = onMic) { Text("🎤", fontSize = 18.sp) }
        }
        IconButton(onClick = onSend) {
            Icon(Icons.Default.Send, contentDescription = "Send", tint = PkLavender)
        }
    }
}

@Composable
internal fun WireGlobe(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "pratiksha-orb")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb-rotation"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb-pulse"
    )

    Canvas(modifier) {
        val r = size.minDimension * 0.31f * pulse
        val c = Offset(size.width / 2f, size.height * 0.43f)
        val glow = r * 1.9f

        // Soft holographic glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x667F45FF),
                    Color(0x334A36D9),
                    Color(0x1142C9FF),
                    Color.Transparent
                ),
                center = c,
                radius = glow
            ),
            radius = glow,
            center = c
        )

        // Glass sphere
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE7D9FF),
                    Color(0xFF9B78FF),
                    Color(0xFF5941C9),
                    Color(0xFF171A48),
                    Color(0xFF090916)
                ),
                center = Offset(c.x - r * 0.35f, c.y - r * 0.42f),
                radius = r * 1.75f
            ),
            radius = r,
            center = c
        )

        // Glass reflections
        drawCircle(
            color = Color(0x557EDBFF),
            radius = r * 0.76f,
            center = Offset(c.x + r * 0.12f, c.y + r * 0.12f),
            style = Stroke(width = 1.3.dp.toPx())
        )
        drawCircle(
            color = Color(0x99E9DCFF),
            radius = r,
            center = c,
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Rotating holographic longitude rings
        for (i in 0 until 4) {
            val angle = Math.toRadians((rotation + i * 45f).toDouble())
            val w = (r * kotlin.math.abs(kotlin.math.cos(angle).toFloat()))
                .coerceAtLeast(r * 0.08f)
            drawOval(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0x2279DFFF),
                        Color(0xFFE2C9FF),
                        Color(0xAA9D76FF),
                        Color(0x3379DFFF)
                    ),
                    startX = c.x - w,
                    endX = c.x + w
                ),
                topLeft = Offset(c.x - w, c.y - r),
                size = Size(w * 2f, r * 2f),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Latitude rings
        for (i in -2..2) {
            val dy = r * i / 3f
            val hw = kotlin.math.sqrt((r * r - dy * dy).coerceAtLeast(1f))
            drawOval(
                color = if (i == 0) Color(0xCCDBCBFF) else Color(0x779A83FF),
                topLeft = Offset(c.x - hw, c.y + dy - hw * 0.17f),
                size = Size(hw * 2f, hw * 0.34f),
                style = Stroke(width = 1.1.dp.toPx())
            )
        }

        // Bright specular highlight
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xDDEFE6FF), Color(0x44D6B8FF), Color.Transparent),
                center = Offset(c.x - r * 0.34f, c.y - r * 0.43f),
                radius = r * 0.48f
            ),
            radius = r * 0.48f,
            center = Offset(c.x - r * 0.34f, c.y - r * 0.43f)
        )
    }
}

@Composable
internal fun Shortcut(icon: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PkPanel)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, color = PkLavender, fontSize = 16.sp)
        Spacer(Modifier.width(6.dp))
        Text(label, color = PkWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PkPanel)
            .padding(12.dp)
    ) {
        Text(title, color = PkMuted, fontSize = 12.sp)
        Text(value, color = PkWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 2)
    }
}
