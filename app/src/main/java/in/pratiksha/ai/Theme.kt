package `in`.pratiksha.ai

import androidx.compose.ui.graphics.Color

// ---------- Colors (har naam sirf EK baar) ----------
internal val PkNight = Color(0xFF090916)
internal val PkPanel = Color(0xFF17152B)
internal val PkViolet = Color(0xFF955BFF)
internal val PkLavender = Color(0xFFC9A8FF)
internal val PkMuted = Color(0xFF9990B8)
internal val PkWhite = Color(0xFFF8F5FF)

internal data class ChatMessage(val role: String, val text: String)
