package `in`.pratiksha.ai

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
                Text("Good morning,", color = PkMuted, fontSize = 20.sp)
                Text("Suchit Bhai ✨", color = PkWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold)
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
        val c = Offset(size.width / 2, size.height / 2 - 12.dp.toPx())
        val line = 1.2.dp.toPx()

        // glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x553D2A75), Color(0x113D2A75), Color.Transparent),
                center = c,
                radius = r * 1.8f
            ),
            radius = r * 1.8f,
            center = c
        )

        // outer ring
        drawCircle(
            color = Color(0x665C3C91),
            radius = r,
            center = c,
            style = Stroke(width = 2.dp.toPx())
        )

        // latitude lines
        for (i in -2..2) {
            val dy = r * i / 3f
            val hw = sqrt((r * r - dy * dy).coerceAtLeast(1f))
            drawOval(
                color = Color(0x88955BFF),
                topLeft = Offset(c.x - hw, c.y + dy - hw * 0.16f),
                size = Size(hw * 2, hw * 0.32f),
                style = Stroke(width = line)
            )
        }

        // longitude lines (ghoomti hui)
        for (k in 0 until 6) {
            val angle = Math.toRadians((rotation + k * 30f).toDouble())
            val w = (r * abs(cos(angle)).toFloat()).coerceAtLeast(1f)
            drawOval(
                color = Color(0xAAC9A8FF),
                topLeft = Offset(c.x - w, c.y - r),
                size = Size(w * 2, r * 2),
                style = Stroke(width = line)
            )
        }
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
