# Pratiksha AI — Android source project

This is a native Kotlin + Jetpack Compose starter project with a Maya-inspired dark/violet dashboard.

## What works in this starter
- Native Compose UI, animated wireframe globe, bottom navigation
- Local conversation history (last 100 messages)
- OpenAI-compatible Chat Completions API configuration in Settings
- API key encrypted with Android Keystore on the device
- Hindi speech recognition launch and Android TextToSpeech
- User-editable memories stored locally
- Scan opens the phone's camera app without requesting camera permission
- GitHub Actions workflow to build a debug APK in the cloud

## Important limitations
- The weather card intentionally does not invent live weather. A weather provider/API has not been connected.
- Gaane and Padhai shortcuts open informational pages; ask for these tasks in Chat.
- Journal is a placeholder, not a complete editor yet.
- AI requires a valid HTTPS OpenAI-compatible endpoint, model name, and API key in Settings.
- A mobile browser such as Claude in Chrome cannot itself compile an APK. GitHub Actions runs the build on a cloud Linux machine.
- This starter has not been compiled in this workspace because Android SDK and Gradle are not installed here. Do not treat this archive as a verified APK.

## Build with GitHub Actions
1. Create a GitHub repository and upload/push the contents of this folder.
2. Open the repository's **Actions** tab.
3. Select **Build Pratiksha AI APK** and press **Run workflow** (or push to `main`).
4. Wait for the workflow to finish successfully.
5. Open that run, scroll to **Artifacts**, and download `PratikshaAI-debug-apk`.
6. Extract the ZIP; install `app-debug.apk` on your Android phone. Android may ask you to allow installation from the browser/files app.

## API configuration
Open app > Settings (top-left menu) and enter:
- Endpoint: e.g. `https://api.openai.com/v1/chat/completions`
- Model: a model your provider account supports
- API key: your own provider key

This is an OpenAI-compatible API client, not a bundled AI model. Provider usage charges and rate limits may apply. Never commit API keys to GitHub. A user-entered key is encrypted at rest on the device, but for a public multi-user app the safer production architecture is your own authenticated backend proxy so users do not need to manage provider keys.

## Project settings
- Application ID: `in.pratiksha.ai`
- Min SDK: 26
- Compile/target SDK: 36
- Java target: 17
