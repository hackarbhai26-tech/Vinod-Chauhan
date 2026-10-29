# JARVIS AI — Advanced Intelligent Android Assistant

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-cyan.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)

An ultra-modern, production-oriented personal cybernetic assistant for Android smartphones. Built with **Kotlin**, **Jetpack Compose (Material 3)**, **MVVM Clean Architecture**, and high-fidelity dark HUD themes with animated circular AI response orb and real-time device telemetry.

---

## 📱 Mandatory APK Download Section

### Download Standalone Packages
* **[Download Debug APK (Universal)](https://github.com/aistudio-apps/jarvis-ai/releases/latest/download/app-debug.apk)** — Direct installable debug build for all 64-bit and 32-bit Android devices.
* **[Download Release APK (R8 Optimized)](https://github.com/aistudio-apps/jarvis-ai/releases/latest/download/app-release.apk)** — Minified, compact build targeting under 30 MB.
* **[View GitHub Releases & Changelog](https://github.com/aistudio-apps/jarvis-ai/releases)**

### Build & Package Specifications
* **Package Name**: `com.aistudio.jarvisai.qnxwz`
* **Version**: `v1.0.0` (Build `#1`)
* **Minimum Android Version**: Android 8.0 (API 24)
* **Target Android Version**: Android 16 (API 36)
* **Build Artifact Path**: `app/build/outputs/apk/debug/app-debug.apk`
* **APK File Size**: 14 MB (Universal Debug APK)
* **SHA-256 Checksum**: `980ce925af92501378fb8797db60b74293abb3ef777260c2563eed55a1bb362a`
* **Build Verification Status**: `SUCCESS`

### Android Installation Instructions
1. Download the `app-debug.apk` file to your device (or export via Settings / AI Studio).
2. In Android Settings, ensure **"Install unknown apps"** is allowed for your browser or file manager.
3. Tap the downloaded APK from your notifications or Downloads directory and select **Install**.
4. Launch JARVIS AI and grant optional Microphone or Camera (Torch) permissions when prompted.

### ⚠️ Resolving Common Installation Errors
* **"Blocked by Play Protect" / "Unsafe app blocked"**:
  Because this is a freshly compiled debug binary signed with a developer keystore, Google Play Protect displays an unknown developer warning.
  👉 **Fix**: Tap **"More details"** (or the down arrow) on the prompt, then tap **"Install anyway"**.
* **"App not installed as package conflicts with an existing package"**:
  A previous test version of the app with a different signature is already installed.
  👉 **Fix**: Uninstall any older version of JARVIS AI from your phone's home screen or settings, then re-install.
* **"There was a problem parsing the package" (Parse Error)**:
  The APK file was corrupted or incomplete during download.
  👉 **Fix**: Re-download the full 14MB APK package or share directly from the app via the "Export / Share APK" button.
* **"Install unknown apps blocked / Restricted Setting"**:
  Android restricts direct APK installation from unauthorized apps.
  👉 **Fix**: Open Android Settings > Apps > Special app access > Install unknown apps > Select your browser/file manager > Toggle "Allow from this source" ON.

---

## ⚡ Key Features

### 1. Futuristic Dark JARVIS Dashboard
* **Animated Circular AI Response Orb**: Concentric multi-ring rotating core, reactive audio frequency/waveform visualization, idle breathing mode.
* **Real-time Live Telemetry**: Live digital clock, date, and battery status (percentage and charging indicator).
* **Hardware Diagnostics**: Real-time RAM memory consumption and internal storage allocation bars.
* **Weather & News Dispatch**: Atmospheric weather sensors and global news summary with vocal speech read-out.
* **14 Distinct HUD Visual Themes**:
  1. *Arc Reactor* (Cyan Neon)
  2. *Hologram Blue* (Cerulean)
  3. *Cyberpunk Amber* (Gold)
  4. *Deep Space Neon* (Phosphor Blue)
  5. *Quantum Matrix* (Matrix Emerald)
  6. *Stark Titanium* (Platinum Silver)
  7. *Crimson Alert* (Warning Red)
  8. *Bio-Synthesizer* (Toxic Green)
  9. *Plasma Core* (Magenta/Violet)
  10. *Orbit Station* (Galactic Sapphire)
  11. *Nebula Violet* (Cosmic Amethyst)
  12. *Golden Sol* (Solar Flare Gold)
  13. *Stealth Shadow* (Monochrome OLED)
  14. *Solar Flare* (Flame Orange)
* **Live Command Terminal**: Scrolling console logging timestamped actions, sensory triggers, and system events.

### 2. Dual-Engine AI Intelligence & Natural Conversation
* **Dual Intelligence Core**:
  * **Local Neural Engine**: Fast, offline NLP parser supporting calculations, device actions, Hindi/English translations, and diagnostics without sending data over the network.
  * **Encrypted Cloud AI (OpenAI / Gemini)**: Optional integration using user-supplied API key via secure HTTPS.
* **Bilingual Voice & Text**: Seamless switching between **English** and **Hindi (हिन्दी)**.
* **Android Text-To-Speech (TTS)**: Pitch, rate (0.6x to 1.6x), and voice selection with instant silence button.
* **Android SpeechRecognizer**: Zero-noise vocal transcription with live microphone amplitude telemetry.
* **Custom Persona & Wake-Word**: Configure assistant name (default: JARVIS) and activation wake-words (*"Hey Jarvis"*, *"Jarvis"*, *"Friday"*, *"Computer"*).

### 3. JARVIS Lab & Sensor Gesture Controls
* **Shake Detection**: Accelerometer motion detection with 10-level sensitivity, cooldown debounce, and configurable actions (*Flashlight, Voice Command, Mute, Emergency Alarm*).
* **Acoustic Clap Detection**: Microphone peak level sensor with live amplitude meter and threshold calibration.
* **Independent Toggles**: Every gesture can be enabled or disabled independently.

### 4. Smart System Automation & Device Controls
* **Hardware Torch**: Flashlight toggle using `CameraManager.setTorchMode`.
* **Audio Level**: Real-time media volume slider.
* **Quick Portals**: Instant shortcuts to Wi-Fi, Bluetooth, Display, Sound, and Notification settings.
* **Consequential Confirmation**: Safety-first confirmation modals prior to placing phone calls, composing WhatsApp dispatches, or deleting files.
* **Scheduled Reminders**: Hydration, Medication, Health, and Custom directives backed by Room SQLite.

### 5. Built-in Scoped Storage File Manager
* Browse authorized application and documents storage.
* Create custom folders and text files.
* Search and sort files.
* Safe deletion with mandatory user confirmation dialog.

### 6. Security, Privacy & Emergency Override
* **Emergency Kill Switch**: One-tap emergency stop that immediately disengages speech recognition, voice synthesis, audio recordings, and background gesture polling.
* **Permissions Dashboard**: Clear transparency and audit of all Android permissions with in-app authorization triggers.
* **Local Data Purge**: Completely wipe all conversation history and cached records on demand.

---

## 🛠️ Build & Development

### Building with Gradle
```bash
# Build the Debug APK
gradle assembleDebug

# Output location
ls -lh app/build/outputs/apk/debug/app-debug.apk

# Run unit tests
gradle :app:testDebugUnitTest
```
