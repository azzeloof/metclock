# Spaceflight Mission Tracker

A native Android application designed to track active and upcoming spaceflight missions. The app provides a live Mission Elapsed Time (MET) clock and countdowns to specific orbital milestones via an in-app dashboard and a persistent lockscreen notification.

## Features

* **Persistent Lockscreen Tracking:** Utilizes an Android Foreground Service to maintain a live-ticking MET clock and milestone countdown on the lockscreen.
* **Live Telemetry Dashboard:** An in-app UI displaying a precise, monospace ticking clock independent of the background service.
* **Dynamic Mission Manifests:** A scrollable vertical timeline generated from mission data, complete with a visual indicator of the current temporal position.
* **Haptic Milestone Alerts:** Configurable vibration alerts that trigger at T-5 minutes and exactly at T-0 for any upcoming orbital burn or event.
* **Dark Mode Aesthetics:** A custom XML-based design system tailored for a space dashboard environment.

## Architecture & Tech Stack

This project is built entirely in Kotlin using standard Android XML views.

* **Minimum SDK:** API 26 (Android 8.0) - Required for `java.time` support.
* **Target SDK:** API 34+ (Android 14) - Fully compliant with modern Foreground Service requirements.
* **Concurrency:** Uses Kotlin Coroutines (`kotlinx.coroutines`) for efficient, non-blocking time calculations updated at 1Hz.
* **Data Layer:** Currently utilizes an in-memory repository (`MissionRepository`) with hardcoded `Instant` timestamps for maximum reliability without network dependency.
* **Persistence:** `SharedPreferences` is used for lightweight user settings (haptic toggles).

### Key Components

* `MissionClock.kt`: A decoupled math engine that consumes a liftoff time and a list of events, outputting a formatted state object.
* `MissionClockService.kt`: The `FOREGROUND_SERVICE_SPECIAL_USE` service responsible for keeping the OS awake and pushing updates to the NotificationManager.
* `TimelineAdapter.kt`: A `RecyclerView` adapter that parses absolute timestamps into local time and dynamically positions the current-event indicator.

## Installation and Build

1. Clone the repository.
2. Open the project in Android Studio (Koala or newer recommended).
3. Sync the project with Gradle files.
4. Build and deploy to an emulator or physical device running Android 8.0+.

*Note: On devices running Android 13 (API 33) or higher, the app will explicitly request `POST_NOTIFICATIONS` permission on launch. On Android 14 (API 34) or higher, the app utilizes the `specialUse` Foreground Service type.*

