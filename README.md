# Shinobi Dojo

**Shinobi** is an offline-first, highly disciplined AI mentor, physical training coach, behavioral analyst, and study companion for Android. Built with Kotlin, Jetpack Compose, and Material 3, the app provides a sanctuary (or "Dojo") for self-discipline, routine optimization, and direct, no-nonsense mentorship.

---

## 🕋 Core Features

*   **Continuous Self-Discipline Dojo**: A direct AI partner that analyzes your behavior, calls out stagnation, and provides high-growth action plans. No empty praise or sugary motivation—pure accountability.
*   **Real-time Google Search Grounding**: Integrates active web-searching toolsets with the Gemini API to construct customized exercise blueprints, workout routines, and fitness knowledge suited strictly to your demands.
*   **Local SQLite Persistence (Room)**: Securely stores daily checking flags, accountability streak counters, study sessions, and chronological messaging lists. Everything is fully offline-ready.
*   **Credentials Hub**: Features an integrated Settings Dashboard to let you load your own private, secure Google Gemini API Key directly on the local storage to bypass default workspace rate limits.
*   **Geometric Adaptive Icon**: Visual brand pairing featuring a stark white shuriken centered on a dark grid with a micro-crimson color palette.

---

## 🛠️ Architecture & Tech Stack

*   **UI Framework:** Jetpack Compose (Material Design 3)
*   **State Architecture:** MVVM (Model-View-ViewModel) with Kotlin Coroutines and unidirectional state flows (`StateFlow`).
*   **Local Database:** Room Persistence Library
*   **Network Service:** Retrofit 2 with Kotlin Serialization
*   **API Backbone:** Gemini Pro developer APIs with Search Tool grounding

---

## 🔑 Custom Key Setting & Internet Support

If the workspace default developer credentials trigger an HTTP `403 Forbidden` error (such as a leaked-key notice):
1.  **Generate a Key**: Go to [Google AI Studio](https://ai.google.dev/) and grab a free personal API key.
2.  **Add to settings**: Open the app, and tap the **Settings Gear** icon in the top right corner.
3.  **Configure Search**: Paste your API Key, enable the "Real-time Google Search" switch, and click **Optimize Training**.

---

## 🏗️ Building & Verification

Verify changes and run local JVM tests on the terminal space:

```bash
# Verify compilation
compile_applet

# Verify local JVM suite
gradle :app:testDebugUnitTest
```
