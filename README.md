# Shinobi
### *Die less. Become more.*

Shinobi is an AI-powered personal growth app that treats your real life as the hardest game you will ever play. It combines a behavioral analyst, study mentor, and fitness coach into one focused experience — not to track your habits, but to tell you the truth about them.

> Most self-improvement apps reward you for opening them. Shinobi rewards you for actually changing.

---

## The Problem

Every productivity app has the same flaw: it trusts whatever you tell it.

You log a workout you skipped. You mark a session complete after 20 minutes of scrolling. The app says "Great job!" and the data becomes a mirror of your self-deception — not your actual behavior. Streaks, points, and badges optimize for daily opens, not real change. And motivational notifications stop working by week three.

The result: you feel productive without becoming capable.

---

## Our Resolve

Shinobi does not congratulate you for showing up. It watches what you actually do over weeks, names the patterns it finds, and gives you one honest, specific thing to work on. No streaks. No leaderboards. No badges for opening the app.

When you miss a workout, it does not lecture you. It says: *"Do 10 pushups right now. That's 4 minutes. The chain stays alive even when the session doesn't."*

When you've planned 90-minute study sessions six times and completed zero, it says exactly that — and asks whether this is a planning problem or an avoidance problem.

The only metric that matters is whether you are becoming stronger, more disciplined, and more capable than you were last month.

---

## Core Features

**Behavioral Analyst Chatbot**
A direct, no-nonsense AI mentor powered by Gemini. It maintains a memory of your patterns across sessions — not just what you report, but what the data suggests. It names avoidance, inconsistency, and self-deception by name, without cruelty and without sugarcoating. Feedback is always specific, never generic.

**Offline-First with Room Persistence**
All your behavioral data, check-ins, workout logs, and study sessions are stored locally using Room database. The app works fully offline. Your history is yours — on your device, not a server.

**Real-Time Google Search Grounding**
Workout plans are not pulled from a static library. When generating physical training blueprints, Shinobi uses real-time Google Search Grounding via Gemini to retrieve current, accurate training methodologies dynamically — so plans reflect what actually works, not what was hardcoded six months ago.

**Four-Axis Progression System**
Progress is tracked across Discipline, Knowledge, Body, and Awareness — updated weekly based on observed behavior, not self-reported wins. Advancement requires sustained change over three or more weeks. One good week is noise. Three good weeks is signal.

**The Minimum Viable Move**
When a session is missed, the app never punishes. It offers the floor — the smallest action that keeps the habit chain alive. This prevents the all-or-nothing abandonment spiral that kills most fitness and study routines.

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose with Material Design 3 |
| Architecture | MVVM with StateFlow |
| Local Database | Room (offline-first persistence) |
| Networking | Retrofit |
| AI | Gemini API with Google Search Grounding |
| Language | Kotlin |

---

## Custom API Key Setup

Shinobi uses the Gemini API. If you hit workspace developer key limits, configure your own private key directly on your device:

1. Go to [Google AI Studio](https://aistudio.google.com) and generate a free Gemini API key
2. Open Shinobi on your device
3. Navigate to **Settings → API Configuration**
4. Paste your key into the **Gemini API Key** field
5. Tap **Save** — the app will use your key for all subsequent requests

Your key is stored locally on your device and never transmitted to any external server other than Google's Gemini endpoint.

---

## Build & Test

**Clone the repository**
```bash
git clone https://github.com/yourusername/shinobi.git
cd shinobi
```

**Build the app**
```bash
./gradlew assembleDebug
```

**Install on connected device**
```bash
./gradlew installDebug
```

**Run local JVM test suites**
```bash
./gradlew test
```

**Run instrumented tests**
```bash
./gradlew connectedAndroidTest
```

---

## Philosophy

Shinobi is not designed to maximize your daily opens. It is designed to make itself unnecessary. A tool that genuinely transforms you should eventually have nothing left to say — because you have already become what you were building toward.

> *"The shinobi does not need permission to train. The shinobi trains because that is what they are."*

---



