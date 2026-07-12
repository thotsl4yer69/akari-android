# Akari — Android architecture guide

Read `README.md` first (design spec). This file maps it to a buildable Kotlin project. Where this file and the prototype disagree, the prototype (`Akari.dc.html`) wins.

## Stack
- Kotlin 2.x, Jetpack Compose (BOM), Material 3 (heavily themed via `compose/Theme.kt` — do not ship default M3 purple anywhere)
- Single module `:app`, single activity, Navigation Compose
- Room (diary data) + DataStore Preferences (settings)
- Health Connect (`androidx.health.connect:connect-client`) — optional integration
- `minSdk 28`, `targetSdk 35`
- **No `INTERNET` permission.** The manifest must not request it. Privacy is a feature: all data stays on device.

## Package layout
```
com.akari.app
├── ui/theme/        Theme.kt (copy from handoff), Font.kt (bundled TTFs)
├── ui/components/   Lantern.kt, EnergyBar.kt, ZoneCard.kt, HrChip.kt, Sheet.kt
├── ui/screens/      Onboarding, Morning, Home, Crash, Trends, History,
│                    Settings, HealthConnect
├── data/            AkariDb.kt, entities, DAOs, PrefsRepository.kt
├── domain/          PacingEngine.kt (all derivations — pure, unit-tested)
└── health/          HealthConnectRepository.kt
```

## Data model (Room)
```kotlin
@Entity data class Day(
  @PrimaryKey val date: LocalDate,
  val startBattery: Int,      // morning check-in 0..100
  val sleep: SleepQuality,    // RESTED / OKAY / POOR / BROKEN
  val zone: Zone,             // GREEN / AMBER / RED (derived, but user-overridable)
  val pem: Boolean = false,   // crash flagged this day
)
@Entity data class SpendEvent(          // every log costs light
  @PrimaryKey(autoGenerate = true) val id: Long,
  val date: LocalDate, val at: Instant,
  val dimension: Dimension,   // PHYSICAL / COGNITIVE / EMOTIONAL
  val amount: Int,            // energy cost
  val kind: String,           // activity | symptom | vitals | food
  val note: String? = null,
)
@Entity data class CrashAction(
  @PrimaryKey(autoGenerate = true) val id: Long,
  val date: LocalDate, val at: Instant,
  val action: CrashKind,      // REST / PEM_FLAG / MEDS
)
```
DataStore prefs: `name`, `restingHr` (default 67), `lanternHue` (argb), `poeticVoice` (bool), `onboardingDone` (bool), `hcConnected` (bool) + granted HC permission set.

## PacingEngine (pure functions — test these)
- `remaining(day) = startBattery − Σ spend.amount`, floor 0. This number IS the lantern.
- `ceiling = restingHr + 15` bpm.
- Zone from battery: ≥70 GREEN "Steady", ≥40 AMBER "Careful", else RED "Low".
- HR tag: `< ceiling−5` within pace (sage) · `< ceiling` near ceiling (akari) · else above ceiling (clay + chip tints clay).
- Trends: last 14 days of `remaining`; bar color sage >60 / akari >34 / clay; crash days get the dot marker.
- Envelope stats: crashes flagged = count(pem), avg spent/day.

## The lantern (get this exactly right)
Compose `Canvas`/Brush port of the prototype's layered radial gradients:
paper shell (`#F7E6C4→#E6D3A6` ellipse), inner glow = lanternHue radial fading
to transparent at ~72%, opacity = `remaining/100`; flame core ember. Energy
changes tween opacity/scale over `StateMs`. One composable, three sizes
(onboarding hero, home hero, history mini 30×38). `lanternHue` flows from
DataStore through a CompositionLocal so **every** lantern recolors (v2 fix).

## Navigation map
```
onboarding(3 steps, skippable) → morning → home ⇄ tabs(trends|history|settings)
home → crash (moon button, full-screen dark, back = "return to the day")
home → log sheet (bottom sheet: activity → symptom/vitals/food sub-sheets)
settings → healthconnect
```
New day at first launch after 4am → morning check-in before home.

## Health Connect
- Read-only: `HeartRateRecord`, `RestingHeartRateRecord`, `SleepSessionRecord`, `StepsRecord`.
- Poll latest HR every ~15s while home is resumed; no background work in v1.
- Not installed / not granted / disconnected ⇒ home shows the dashed "No wearable linked — pacing by feel · Connect" row (v2). **Never simulate bpm.** The prototype's ticking HR is demo-only.
- Sleep quality pre-fills morning check-in when available; user can override.

## Seed / demo data
The prototype seeds 14 days of history + today's state (`seededDay()` in the DC source). Port it behind `BuildConfig.DEBUG` so designers can review real screens; release builds start empty. Empty states ARE designed (README “v2.1” + `reference/11–14`): zero-spend Home line, Trends “envelope needs a few days” card, History unlit-lantern state. The prototype's Settings → “First-run state” toggle is a design-review affordance only — do not ship it.

## Build & ship
1. Android Studio → New empty Compose project, apply layout above; drop in `compose/Theme.kt`, bundle Newsreader + Zen Kaku Gothic New TTFs (Google Fonts, OFL) in `res/font/`.
2. Build order that works well with Claude Code: PacingEngine + tests → Theme/Fonts → Lantern → Morning → Home → Log sheets → Crash → Trends/History → Settings/HC.
3. Debug APK: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`.
4. CI (optional): GitHub Actions `gradle/actions/setup-gradle`, run `assembleDebug` + `testDebugUnitTest`, upload the APK as an artifact on every push.
5. Before any release build, walk `QA_CHECKLIST.md` top to bottom on a real device at 200% font scale.
