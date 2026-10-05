# Habit Counter

A tiny Android app (Kotlin + Jetpack Compose) for counting daily habits. Built as a first project to practice publishing to Google Play.

- Builds run on GitHub Actions (`.github/workflows/build.yml`); no local Android Studio needed.
- Each push produces `app-debug-apk` (install on a phone to test). Once signing secrets exist, it also produces `app-release-aab` (what Google Play accepts).
