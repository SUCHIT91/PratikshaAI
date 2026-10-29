"""Voice layer hooks. Android pe TTS/STT yahan se jodenge.

Note: kisi bhi insaan ki awaaz ko clone karne ke liye us insaan ki
spasht ijaazat (consent) zaroori hai. Maya ki awaaz copy karne ka
koi code yahan nahi diya gaya — sirf ek generic voice interface hai.
"""

# User-approved voice profile ka placeholder config.
# Jab tak `approved` False hai, profile active nahi hoga aur default voice chalegi.
VOICE_CONFIG = {
    "profile": "default",   # approved profile ka naam (abhi sirf "default")
    "approved": False,      # sirf user ki spasht ijaazat ke baad True karein
    "approved_by": None,    # kis ne approve kiya (naam/ID)
}

DEFAULT_PROFILE = "default"


def load_voice_profile(config: dict = VOICE_CONFIG) -> str:
    """Config se voice profile naam lautata hai.

    Profile tabhi active hota hai jab `approved` True ho AND `approved_by`
    bhi bhara ho (kisi ne explicitly approve kiya ho). Warna 'default'.
    """
    if config.get("approved") and config.get("approved_by") and config.get("profile"):
        return config["profile"]
    return DEFAULT_PROFILE


class VoiceInterface:
    def __init__(self, enabled: bool = False, voice_profile: str | None = None):
        self.enabled = enabled
        # Caller ne profile diya to wahi; nahi to config se load (approved hone par hi custom).
        self.voice_profile = voice_profile or load_voice_profile()

    def speak(self, text: str) -> None:
        """Reply bolna. Android TTS engine yahan call hoga; abhi sirf console par dikhata hai."""
        if self.enabled:
            print(f"[voice:{self.voice_profile}] {text}")

    def listen(self) -> str | None:
        """Awaaz se input. Abhi speech engine configure nahi hai, isliye None lautata hai.

        None ka matlab: caller text input (keyboard) par fallback kare.
        """
        return None
