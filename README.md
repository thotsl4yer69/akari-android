# Akari (明かり)

A native Android energy diary for ME/CFS — pacing, PEM tracking, and a lantern that dims as you spend the day's light. Local-only by design: no account, no cloud, no `INTERNET` permission.

**Status:** design-complete, implementation pending. This repo starts as a design bundle + build brief; the Android app is generated from it.

## Layout
- `CLAUDE.md` — build brief (Claude Code reads this automatically)
- `design/` — the full handoff: interactive prototype (`Akari.dc.html` — open in any browser), design spec (`README.md`), `ARCHITECTURE.md`, `QA_CHECKLIST.md`, Compose theme starter (`compose/Theme.kt`), and 14 reference screenshots (`reference/`)
- `.github/workflows/android.yml` — CI; builds a debug APK on push once the Gradle project exists

## Build the app
1. Open this repo in [Claude Code](https://claude.com/claude-code) and say: **"Read CLAUDE.md and build the app."** Iterate until `./gradlew assembleDebug testDebugUnitTest` passes.
2. Or manually: Android Studio → new empty Compose project in this repo → follow `design/ARCHITECTURE.md`.

Debug APK lands at `app/build/outputs/apk/debug/app-debug.apk` → `adb install` or sideload. Test Health Connect on a real device with the Health Connect app installed.

Before sharing any build, walk `design/QA_CHECKLIST.md` on-device at 200% font scale — for this audience, accessibility is the product.
