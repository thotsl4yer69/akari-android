# CLAUDE.md — build brief for Claude Code

You are building **Akari (明かり)**, a native Android energy diary for people with ME/CFS. The complete design lives in `design/` — it is the **source of truth**. Do not edit anything in `design/`; build to match it.

## Read first, in this order
1. `design/README.md` — full design spec: every screen, state, token, motion rule, and copy rule (v2.1 includes empty states).
2. `design/ARCHITECTURE.md` — the stack and structure you must follow exactly.
3. `design/QA_CHECKLIST.md` — accessibility non-negotiables + per-screen acceptance criteria. This is the definition of done.
4. `design/compose/Theme.kt` — colors/type/shapes/dims/motion as Kotlin. Copy into `app/src/main/java/com/akari/app/ui/theme/` and build on it.
5. `design/Akari.dc.html` — the interactive prototype source. For precise behavior, read `class Component`: `renderVals()` maps state → view; `seededDay()`, `presetData()`, `restData()`, `historyData()`, `triggerData()` hold demo data (port behind `BuildConfig.DEBUG`).
6. `design/reference/*.png` — 14 pixel captures (01–10 main flow, 11–14 first-run empty states). Match them.

## Hard constraints (from the spec — do not negotiate)
- Kotlin 2.x, Jetpack Compose, Material 3 themed from `Theme.kt` — **no default M3 purple anywhere**, no dark theme (crash mode is its own palette).
- Single `:app` module, single activity, Navigation Compose; package layout exactly as `ARCHITECTURE.md`.
- Room + DataStore; **no `INTERNET` permission** in the merged manifest — privacy is a feature.
- Health Connect read-only; never simulate bpm; no wearable ⇒ the dashed "No wearable linked" row.
- Fonts: bundle Newsreader + Zen Kaku Gothic New TTFs in `res/font/` (offline app).
- minSdk 28, targetSdk 35.
- Release builds start EMPTY and must show the three designed empty states. The prototype's Settings "First-run state" toggle is design-review only — **do not ship it**.

## Build order (each step compiles before the next)
1. Gradle scaffold + theme + fonts.
2. `domain/PacingEngine.kt` — pure functions (remaining, ceiling, zones, HR tags, trend colors) **with unit tests**; derivation rules are in `ARCHITECTURE.md`.
3. `ui/components/Lantern.kt` — one composable, three sizes, hue via CompositionLocal; get the layered radial gradients exactly right (the emotional core of the app).
4. Screens: Morning → Home → Log sheets → Crash → Trends → History → Settings → Health Connect → Onboarding.
5. Wire Room/DataStore persistence + new-day-after-4am logic.
6. Walk `QA_CHECKLIST.md`; fix everything it catches.

## Definition of done
`./gradlew assembleDebug testDebugUnitTest` passes; APK at `app/build/outputs/apk/debug/app-debug.apk`; every box in `design/QA_CHECKLIST.md` checkable on a real device at 200% font scale; screens visually match `design/reference/`.

CI: `.github/workflows/android.yml` builds a debug APK artifact on push once `gradlew` exists at the repo root.
