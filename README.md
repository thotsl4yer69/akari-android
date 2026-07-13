# Akari (明かり)

A native Android energy diary for ME/CFS — pacing, PEM tracking, and a paper lantern that dims as you spend the day's light. Local-only by design: no account, no cloud, no `INTERNET` permission, and OS backup disabled (the in-app JSON export is the migration path).

**Status:** implemented. Kotlin 2 + Jetpack Compose, single `:app` module; CI builds the APK and runs the full test suite (including an emulator smoke walk) on every push.

## Layout
- `app/` — the Android app (Compose UI, Room + DataStore persistence, optional read-only Health Connect)
- `design/` — the source of truth: interactive prototype (`Akari.dc.html` — open in any browser), design spec (`README.md`), `ARCHITECTURE.md`, `QA_CHECKLIST.md`, Compose theme starter, and 14 reference screenshots. Do not edit; build to match.
- `.github/workflows/android.yml` — CI: `assembleDebug` + unit tests, then an API 34 emulator smoke test that walks every screen and uploads screenshots

## Build & install
```
./gradlew assembleDebug testDebugUnitTest
```
Debug APK lands at `app/build/outputs/apk/debug/app-debug.apk` → `adb install` or sideload. Every push also produces the `akari-debug-apk` artifact on the Actions run. Test Health Connect on a real device with the Health Connect app installed.

Fresh installs start empty in every build type, showing the three designed first-run states.

Before sharing any build, walk `design/QA_CHECKLIST.md` on-device at 200% font scale — for this audience, accessibility is the product.
