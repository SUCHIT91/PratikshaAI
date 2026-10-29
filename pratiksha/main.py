"""Pratiksha AI — basic text assistant (Android-friendly Python base)."""

from voice import VoiceInterface
from brain import process_input


class PratikshaAssistant:
    def __init__(self, voice_enabled: bool = False):
        self.name = "Pratiksha"
        self.voice = VoiceInterface(enabled=voice_enabled)

    def respond(self, text: str) -> str:
        reply = process_input(text)
        self.voice.speak(reply)
        return reply

    def run(self):
        print(f"{self.name}: Namaste! Main Pratiksha hoon. Likhiye ya 'exit' bolein.")
        while True:
            try:
                user_text = input("Aap: ").strip()
            except (EOFError, KeyboardInterrupt):
                print(f"\n{self.name}: Alvida!")
                break
            if not user_text:
                continue
            if user_text.lower() in ("exit", "quit", "bye"):
                print(f"{self.name}: Alvida! Phir milenge.")
                break
            print(f"{self.name}: {self.respond(user_text)}")


if __name__ == "__main__":
    PratikshaAssistant().run()
