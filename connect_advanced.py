from pathlib import Path

main = Path("app/src/main/java/in/pratiksha/ai/MainActivity.kt")
pages = Path("app/src/main/java/in/pratiksha/ai/Pages.kt")

s = main.read_text()
p = pages.read_text()

# Add required Compose state imports
if "import androidx.compose.runtime.mutableStateMapOf" not in s:
    s = s.replace(
        "import androidx.compose.runtime.mutableStateOf",
        "import androidx.compose.runtime.mutableStateMapOf\nimport androidx.compose.runtime.mutableFloatStateOf\nimport androidx.compose.runtime.mutableStateOf",
        1
    )

# Add persistent advanced settings state
anchor = '    var page by remember { mutableStateOf("Home") }'
if anchor not in s:
    raise SystemExit("ERROR: page state anchor not found; no files changed.")

state_code = '''
    val advancedSettings = remember {
        mutableStateMapOf<String, Boolean>().apply {
            listOf(
                "theme", "appearance", "behaviour", "voice_guardian",
                "face_recognition", "emergency_sos", "touch_guard",
                "screen_lock", "permissions", "event_triggers"
            ).forEach { key ->
                this[key] = prefs.getBoolean("advanced_$key", false)
            }
        }
    }
    var typingSpeed by remember {
        mutableFloatStateOf(prefs.getFloat("typing_speed", 1f))
    }
'''
if "val advancedSettings = remember" not in s:
    s = s.replace(anchor, anchor + "\n" + state_code, 1)

# Add Advanced route before Settings
anchor = '                        "Settings" -> SettingsPage('
if anchor not in s:
    raise SystemExit('ERROR: Settings route anchor not found; no files changed.')

advanced_route = '''                        "Advanced" -> AdvancedSettingsPage(
                            settings = advancedSettings,
                            onSettingChange = { key, value ->
                                advancedSettings[key] = value
                                prefs.edit().putBoolean("advanced_$key", value).apply()
                            },
                            typingSpeed = typingSpeed,
                            onTypingSpeedChange = { speed ->
                                typingSpeed = speed
                                prefs.edit().putFloat("typing_speed", speed).apply()
                            },
                            onBack = { page = "Menu" },
                            onOpenApiSettings = {
                                previousPage = "Advanced"
                                page = "Settings"
                            }
                        )

'''
if '"Advanced" -> AdvancedSettingsPage(' not in s:
    s = s.replace(anchor, advanced_route + anchor, 1)

# Add menu item
old = '"ASSISTANT" to listOf("Sub-agents", "Skills", "Settings", "Yaadein")'
new = '"ASSISTANT" to listOf("Sub-agents", "Skills", "Advanced", "Settings", "Yaadein")'
if old not in p:
    raise SystemExit("ERROR: assistant menu anchor not found; no files changed.")
p = p.replace(old, new, 1)

# Add icon
old_icon = '                            "Settings" -> "⚙"'
if old_icon not in p:
    raise SystemExit("ERROR: Settings icon anchor not found; no files changed.")
if '"Advanced" -> "✧"' not in p:
    p = p.replace(old_icon, '                            "Advanced" -> "✧"\n' + old_icon, 1)

main.write_text(s)
pages.write_text(p)

print("SUCCESS: Advanced Settings connected.")
print("SUCCESS: Preferences and typing speed will persist.")
print("SUCCESS: Advanced menu item added.")
