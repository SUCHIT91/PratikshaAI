"""Placeholder processing layer. Real AI model yahan jodna hai baad me."""


def process_input(text: str) -> str:
    """Abhi ek simple placeholder: input ko samajh ke generic reply deta hai."""
    lowered = text.lower()
    if any(word in lowered for word in ("hello", "hi", "namaste")):
        return "Namaste! Aap kaise hain? Main aapki kaise madad kar sakti hoon?"
    if "naam" in lowered or "name" in lowered:
        return "Mera naam Pratiksha hai."
    return f"Aapne kaha: '{text}'. Abhi main placeholder mode me hoon, jald hi poori samajh aayegi."
