import os
import asyncio
import httpx
import edge_tts

# Textos calibrados para durar exactamente 60 segundos en total (1800 frames a 30 fps)
SCENES_SCRIPT = {
    "scene1_hook": (
        "¿Cuánto dinero y tiempo pierde tu negocio cada vez que un cliente paga con Yape o Plin? "
        "Jefe, ¿le llegó el pago? Espera que mire mi teléfono... Y mientras tanto, los clientes esperando en la fila."
    ),
    "scene2_hero": (
        "¡Se acabaron las llamadas y las capturas falsas por WhatsApp! "
        "Te presentamos TOKE: la app Android que conecta los cobros del dueño con todo el equipo de trabajo al instante."
    ),
    "scene3_demo": (
        "El cliente paga con QR. El celular del dueño detecta la notificación bancaria al segundo, "
        "y el teléfono del trabajador recibe la confirmación con aviso de voz: "
        "¡Pago recibido! Cero demoras, cero dudas."
    ),
    "scene4_benefits": (
        "Atiende tres veces más rápido en caja. Y lo mejor: es cien por ciento seguro, "
        "sin contraseñas de banco ni acceso a tus cuentas."
    ),
    "scene5_cta": (
        "Moderniza tu negocio hoy mismo. "
        "Descarga TOKE para Android completamente gratis en tokelabs.pages.dev. "
        "¡Cobros rápidos, seguros y en equipo!"
    ),
}

async def generate_edge_tts(text: str, output_path: str, voice: str = "es-PE-AlexNeural"):
    """Genera audio con Microsoft Edge Neural TTS (Nube, 48kHz, voz peruana auténtica)"""
    # rate="+16%" para ritmo comercial ágil y dinámico
    communicate = edge_tts.Communicate(text, voice, rate="+16%", pitch="+0Hz")
    await communicate.save(output_path)
    print(f"[Edge-TTS] Generado: {output_path} ({voice})")

def generate_fish_audio(text: str, output_path: str, api_key: str, reference_id: str = None):
    """Genera audio con Fish Audio API (Free tier: s2.1-pro-free)"""
    url = "https://api.fish.audio/v1/tts"
    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json",
    }
    payload = {
        "text": text,
        "model": "s2.1-pro-free",
        "format": "mp3",
    }
    if reference_id:
        payload["reference_id"] = reference_id

    response = httpx.post(url, headers=headers, json=payload, timeout=60.0)
    response.raise_for_status()
    with open(output_path, "wb") as f:
        f.write(response.content)
    print(f"[Fish-Audio] Generado: {output_path} (model: s2.1-pro-free, voice: {reference_id or 'default'})")

async def main():
    os.makedirs("public/audio", exist_ok=True)
    fish_api_key = os.environ.get("FISH_API_KEY")
    fish_voice_id = os.environ.get("FISH_VOICE_ID")

    for key, text in SCENES_SCRIPT.items():
        output_file = f"public/audio/{key}.mp3"
        if fish_api_key:
            print(f"Usando Fish Audio para {key}...")
            try:
                generate_fish_audio(text, output_file, fish_api_key, fish_voice_id)
            except Exception as e:
                print(f"Error con Fish Audio ({e}), cayendo a Edge Neural TTS...")
                await generate_edge_tts(text, output_file)
        else:
            await generate_edge_tts(text, output_file)

    # Audio completo unificado
    full_text = " ".join(SCENES_SCRIPT.values())
    full_output = "public/audio/voiceover_full.mp3"
    if fish_api_key:
        try:
            generate_fish_audio(full_text, full_output, fish_api_key, fish_voice_id)
        except Exception:
            await generate_edge_tts(full_text, full_output)
    else:
        await generate_edge_tts(full_text, full_output)

    print("\n¡Todos los audios fueron generados exitosamente en video/public/audio/!")

if __name__ == "__main__":
    asyncio.run(main())
