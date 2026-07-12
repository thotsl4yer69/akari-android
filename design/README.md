# Handoff: Akari (明かり) — gentle pacing for ME/CFS

## Overview
Akari is a native Android energy diary for **symptom-contingent pacing** in ME/CFS. The day's energy is a **paper lantern (chōchin)** that glows in the morning and softly dims as energy is spent. The goal is to stay within the energy envelope and prevent Post‑Exertional Malaise (PEM) — never to do more. This bundle is the **design** for the app: onboarding, morning intention, the lantern home, activity + rest logging, one‑tap PEM flag, and crash mode.

> Personal diary, not medical advice or a treatment device.

## About the Design Files
The file in this bundle (`Akari.dc.html`) is a **design reference created in HTML** — a fully interactive prototype showing the intended look, motion, and behavior. **It is not production code to copy.** The task is to **recreate it in the target Android app** using that project's established stack and patterns:

- **Kotlin + Jetpack Compose** (Material 3, custom washi/Akari theme)
- **Room** for offline diary storage
- **Health Connect** client (wearable vitals, read‑only, optional)
- **No network access at all** — the app has no `INTERNET` permission

The environment already exists, so implement the designs there. Do **not** port the HTML/JS; translate the visual system, state model, and interactions into idiomatic Compose.

## Fidelity
**High‑fidelity.** Colors, typography, spacing, radii, motion, and copy are final and intentional. Recreate the UI faithfully. The one thing to get *exactly* right is the lantern and its energy‑driven dimming — it is the emotional core.

## What’s new in v2 (Jul 2026) — this bundle
- Lantern warmth (`lanternHue`) recolors **every** lantern — onboarding, home, History minis — not just home.
- Trends envelope marks crash days with a small clay dot + “crash day” legend.
- History rows expand inline to a day detail (spend split bar + Phys/Cog/Emo totals) — replaces the old toast.
- Crash done‑labels end in a quiet check (“Resting ✓”).
- Home shows an honest “No wearable linked” row when Health Connect is off — never simulated bpm.
- Morning slider thumb clamps inside the track at 0/100; pressed buttons/tiles scale 0.97–0.98.

### v2.1 — designed empty states
- **Home, nothing logged yet**: the spend bar + legend are replaced by one italic serif line — poetic “Nothing spent yet — the day is still whole.” / plain “Nothing logged yet.” The card, header, and “0 of {start}” figure stay.
- **Trends, first days**: chart, stats, and triggers are replaced by a single card — four ghost bars (washi2, last one akari @ 35% = today forming), serif “The envelope needs a few days”, caption “After two or three lit lanterns, your pattern begins to show.” (plain: “Charts appear after a few days of entries.”)
- **History, no days**: centered unlit lantern (glow 10%), serif “No days here yet”, caption “When tonight comes, today will settle here — your first lantern.” (plain: “Days appear here after your first evening.”)
- Ship these exactly: release builds start empty (see ARCHITECTURE). In the prototype, preview them via Settings → **Preview · prototype only → First-run state** (dashed card — a design-review affordance, do NOT port it), or the `firstRun` tweak.

## How to view the prototype
Open `Akari.dc.html` in a browser (`support.js` must sit beside it). It opens on onboarding. To jump straight to a screen, it exposes three tweakable props (see the `<script data-dc-script data-props>` block): `startScreen` (`onboarding|morning|home|trends|history|settings|crash`; `healthconnect` also routes as a Settings sub‑screen), `lanternHue` (light warmth), `poeticVoice` (copy tone). All logic lives in the `class Component` block; all state and computed values are in `renderVals()`.

---

## Design Tokens

### Color — washi (paper) light theme
| Token | Hex / value | Use |
|---|---|---|
| `washi` | `#F1E7D4` | app background (warm paper) |
| `washi2` | `#E9DCC4` | inset tracks, empty bars |
| `card` | `#F7F0E2` | cards, sheets, inputs |
| `sumi` | `#2D2A22` | primary text / ink, primary buttons |
| `sumi2` | `#6D6353` | secondary text |
| `sumi3` | `#877A62` | faint labels, eyebrows, timestamps (darkened for AA‑large contrast, ~3.4:1 on paper) |
| `line` | `rgba(45,42,34,0.10)` | hairline dividers |
| `line2` | `rgba(45,42,34,0.17)` | stronger borders |
| `akari` | `#EBA950` | the light — lantern glow, amber/“Careful” |
| `ember` | `#D9793F` | deep warm — tassel, heart icon |
| `ai` (indigo/藍) | `#45688A` | **Cognitive** effort, links, meds |
| `sage` | `#7E9A6E` | **go / rest**, “Steady”, “within pace” |
| `clay` | `#C4745A` | **Physical** effort, “Low”, PEM/over‑ceiling |
| `plum` | `#93627E` | **Emotional** effort |

Effort is always split three ways with fixed colors: **Physical = clay, Cognitive = ai (indigo), Emotional = plum.** Keep this mapping everywhere (cost bars, legends, spent split).

### Color — crash mode (near‑dark)
- Background: radial gradient `#241b14` (30% from top) → `#140f0b` → `#0d0a07`.
- Text: `#e8d9c4` (primary), `#9a8b78` (secondary/italic), `#6a5d4c` (whisper).
- Faint ember glow: radial `rgba(217,121,63,0.55)` → transparent, pulsing.

### Typography
Two families, both on Google Fonts (bundle as `res/font`):
- **Newsreader** (serif) — display, screen titles, the poetic/italic voice, and the big energy number. Weights 400/500; use the **italic** for intimate lines.
- **Zen Kaku Gothic New** (Japanese humanist sans) — all UI, labels, buttons, numbers/data. Weights 400/500/700. Distinctive and calm; do **not** substitute Roboto/Inter.

Scale (sp ≈ px): wordmark 44/500 serif · screen title 25–32/500 serif · sheet/onboarding H2 24–29/500 serif · **energy figure 52 serif** · poetic italic 16–19/400 serif · body 14–15/400 sans · chip/meta 12–13 sans · eyebrow label 12 sans **UPPERCASE, letter‑spacing 0.18–0.22em, color sumi3**. Use **tabular figures** for times and numeric values.

### Radius / shape
phone screen `36` · cards `16` · bottom sheet `26` (top corners only) · buttons `16` · chips/pills `20–24` · crash buttons `22`.

### Motion
- `breathe` 6.5s ease‑in‑out infinite — lantern scale 1 → 1.014.
- `flicker` 3.4s ease‑in‑out infinite — flame hotspot opacity/scale jitter.
- Bottom sheet in: 0.34s `cubic-bezier(.22,.8,.3,1)`; scrim fade 0.25s.
- Screen enter: `rise` 0.5s (opacity + 10px up) / `softIn` fade.
- Toast: auto‑dismiss ~2.6s, slide‑up fade.
- Crash ember: `emberPulse` 5s.
- Press feedback: primary buttons and log tiles scale to 0.97–0.98 while pressed (hue swatches 0.92) — instant, no bounce.
Keep everything gentle and low‑stimulation. No bounce, no bright flashes (crash mode especially).

---

## The Lantern (spec — build first)
A vertically‑oval chōchin whose **brightness = remaining energy (0–100)**. Morning sets brightness to the day's battery; each logged activity dims it. A low‑battery day literally starts as a dimmer lantern.

Structure (top→bottom): cord → dark wood top cap → **body** → dark wood bottom cap → ember tassel + bead. Body:
- Size ≈ 172 × 216; oval via border‑radius `46% / 44%`.
- Base paper: radial gradient `#fdf4e2` (center) → `#f0dcae` (60%) → `#e6ce99` (edge).
- **Glow overlay** (the energy signal): radial `akari` → transparent(72%); `opacity = 0.28 + energy/100 × 0.68`.
- **Flame hotspot**: small blurred white‑amber radial at ~47%, `flicker` animation.
- **Bamboo rings**: horizontal repeating lines `rgba(70,45,15,0.11)` 1px every 14px.
- **Barrel shading**: horizontal linear gradient darkening left/right edges (~22% inset).
- **Halo** behind body: radial `akari` → transparent; `scale = 0.55 + energy/100 × 0.68`, `opacity = 0.10 + energy/100 × 0.5`, blurred.

**Compose approach:** `val e by animateFloatAsState(energy/100f)`. Draw with `Canvas`/`Brush.radialGradient`; drive glow alpha and halo scale from `e`; `rememberInfiniteTransition` for breathe/flicker. Crash mode reuses the shape reduced to a dim coal (body `#6e3f1c → #2a1c11`, ember pulse).

---

## Screens / Views

### 1. Onboarding (first‑run) — 3 cards, pager + dots, dark primary CTA
- **Card 0 — Welcome:** bright decorative lantern, `明 か り` (letter‑spacing 0.5em), **Akari** 44 serif, italic tagline “A gentle place to pace your energy — and to stay within your own light.”
- **Card 1 — How it helps:** eyebrow “HOW IT HELPS”; H2 “Your energy is a lantern. It glows in the morning and dims as the day is spent.”; body about staying within the envelope; a **clay ⚑ callout**: one tap marks a crash; because PEM is delayed 12–72h, Akari looks back over the 48h before to reveal triggers you can't feel.
- **Card 2 — Make it yours:** name input (**optional, self‑owned — the diary is for the signed‑in user; no third‑person references anywhere**); resting‑HR number input → shows **ceiling = resting + 15**; indigo privacy note: everything stays on the phone, no account, no cloud, no internet.
- CTA: “Continue” → “Light your first day” on last card. “Skip introduction” on cards 0–1.

### 2. Morning intention (scrollable)
- Eyebrow date · serif greeting (time‑based) · italic “How full is the lantern right now? Be honest — a low day is not a failure.” (time‑neutral, to match the time‑based greeting)
- **Traffic light**: 3 cards *Steady / Careful / Low* (sage / akari / clay dot). Selecting sets zone + snaps battery (85 / 60 / 28) and shows a colored glow ring.
- **Fine‑tune slider**: custom track, gradient clay→akari→sage, draggable thumb (border = current zone color), live value “NN / 100”. Dragging sets battery and derives zone (≥70 Steady, ≥40 Careful, else Low). Thumb stays fully inside the track at 0 and 100.
- **Sleep**: 4 pills *Broken / Poor / Okay / Rested* (selected = filled sumi).
- Primary “Light the lantern” → seeds the day (Woke + Morning intention entries) and opens Home with `startEnergy = energy = battery`.

### 3. Lantern home (the hero)
Column: scrollable content + **sticky bottom bar**.
- Header: eyebrow date · lowercase serif greeting (with name if set) · round crash‑mode button (crescent, top‑right).
- **Lantern** + below it: italic mood line (Bright / Softening / Low — tend gently / Nearly out / Please rest, thresholds 66/42/20/4) and the **energy figure** “NN” 52 serif + “of NN light left”.
- **HR ceiling chip**: `♥ {hr} bpm · resting {resting} · ceiling {resting+15}` + right tag *within pace / near ceiling / above ceiling* (sage / akari / clay). Over‑ceiling tints the chip clay.
- **No‑wearable state** (default until Health Connect is connected): the chip is replaced by a quiet dashed‑border row — outline heart, “No wearable linked — pacing by feel”, indigo CONNECT → Health Connect screen. Never show simulated bpm when disconnected.
- **Spent today** card: total “NN of {start}”, a single stacked bar split clay/ai/plum by summed physical/cognitive/emotional, legend with per‑dimension totals.
- **PEM banner** (when flagged): clay ⚑ “PEM flagged · {time} — Rest is the work today. Akari will look back 48h for what led here.”
- **Today** section: header + “Flag PEM” pill; a timeline of entries (time · colored dot · name · optional italic sub · right badge). Badge/dot per kind: activity = ember dot + “−N”; rest = sage “rest”; pem = clay “PEM”; meds = ai “meds”; wake/intention = markers. Footer credo “Rest is not idleness. Savasana counts.”
- Crash mode opens from the header crescent (top‑right). Day‑logging is now app‑wide via the tab‑bar **＋** (see *App shell & tab bar*); content scrolls above the bar.

### 4. Activity logging (modal bottom sheet)
Handle · serif title “What did you do?” · italic “One tap logs it and dims the lantern.” · 2‑col grid of **10 presets** (icon, `−total`, name, 3‑seg clay/ai/plum cost bar) · effort legend. **One tap = log immediately**, subtract `total` from energy, close sheet, show toast, dim lantern. Presets & effort splits (Physical/Cognitive/Emotional):
`Shower 5/1/1` · `Cook a meal 4/3/1` · `Short walk 6/1/1` · `Phone call 1/3/3` · `Screen time 1/4/1` · `Errand out 5/3/3` · `See a friend 2/3/5` · `Housework 5/2/1` · `Drive 2/4/2` · `Appointment 3/3/4`.

### 5. Rest logging (modal bottom sheet)
Serif “Rest is the work” · italic “Choose a kind of rest — savasana counts.” · 2‑col sage‑tinted cards: **Lie down, Savasana, Nap, Meditate, Quiet sit, Nothing at all.** Logs a rest entry (sub “savasana counts” for Savasana, else “a deliberate rest”); **does not reduce energy** — rest is a deliberate act, logged as data.

### 6. PEM flag
One tap (Home “Flag PEM” pill, or crash mode). Records a `pem` entry, shows the Home banner, sets `pem = {time}`. Idempotent for the day. This is the most valuable data point — it anchors 48h trigger look‑back.

### 7. Crash mode (near‑dark)
Full‑screen dark overlay (covers status bar). Faint pulsing ember lantern; serif “You're in a crash.” + italic “This is enough. Nothing else is asked of you.” **Four large 2×2 buttons (~128 tall):** *I'm resting · Flag PEM · Took meds · Back to light.* **Silent confirmation** — a tapped action fills softly, the label switches to a done state with a quiet check (“Resting ✓” / “Flagged ✓” / “Meds ✓”), whisper text reads “logged, quietly”; **no sound, no toast bounce**. Rest/Meds/PEM write entries; Back to light exits to Home.

---

## App shell & tab bar
Persistent bottom tab bar on the four main screens — **Today · Trends · ＋ · History · Settings** (washi `card` bar; active tab `sumi`, inactive `sumi3`; center raised **＋** FAB in `sumi`). Hidden on onboarding, morning, crash, and while a sheet is open. The **＋** opens the Log sheet.

### 8. Trends
Eyebrow “Last 14 days” + title. **Energy‑envelope** bar chart (14 bars; height = that day's remaining light; color sage/akari/clay by level, opacity by level); crash days get a small clay dot under their bar, with a “· crash day” legend top‑right of the card. Two stat cards: **crashes flagged** (clay) and **avg spent/day**. **Trigger detection** — activities most often logged in the 48h before a crash, ranked with indigo bars + “N× before crashes”, and a “patterns, not proof” caption.

### 9. History
“Your diary” list of past days. Each row: a **mini glowing lantern** (glow = that day's remaining light), day + date, “Slept X · spent Y of Z”, a **PEM** badge on crash days, and the remaining‑light figure. Tap → the row expands inline: a stacked clay/ai/plum bar of that day's spend + per‑dimension totals; the open row gets the stronger `line2` border. Tap again to collapse.

### 10. Settings
**Profile** (name; resting HR → ceiling). **Wearable** → Health Connect row (status/chevron). **The light**: lantern‑warmth swatches + gentle‑voice toggle (these mirror the `lanternHue` / `poeticVoice` props); warmth applies globally — home, onboarding, and History mini‑lanterns all follow. **Data — stays on this phone**: Export for doctor (CSV), Back up everything (JSON), Restore. About/privacy line.

### 11. Health Connect (sub‑screen of Settings)
Back‑to‑Settings header. Read‑only explanation. Permission rows with toggles — **Heart rate, Resting heart rate, Sleep, Steps** — dots + switches turn sage when connected. Connect / Disconnect button. Pacing‑ceiling note (resting + 15). Community‑wearable note (Garmin Body Battery; Visible + Polar).

### 12. Log sheet (the ＋)
Chooser grid of six: **Activity, Rest, Symptom, Vitals, Food & meds, Flag PEM**. Activity/Rest open the preset grids (screens 4–5); a back arrow returns to the chooser.
- **Symptom** — 1–5 severity (plum) + tag chips (Fatigue, Brain fog, Sore throat, Aches, Dizziness, Headache, Unrefreshing sleep) → logs a `symptom` entry.
- **Vitals** — HR / BP (sys‑dia) / temp fields → `vitals` entry.
- **Food & meds** — two note fields → `note` entry.
New timeline kinds & dots: symptom = plum, vitals = ai, note = sumi3.

---

## State Management
Single source of truth (map to a `ViewModel` + `StateFlow`):
- `screen`: `onboarding | morning | home | trends | history | settings | healthconnect | crash`; `sheet`: `null | log | activity | rest | symptom | vitals | foodmeds`.
- Onboarding: `onbStep 0..2`, `name`, `resting` (→ `ceiling = resting + 15`).
- Morning: `zone (green|amber|red)`, `battery 0..100` (zone thresholds 70/40; zone presets 85/60/28), `sleep 1..4`.
- Day: `startEnergy`, `energy` (`= battery` at light; `energy = max(0, energy − activity.total)` per log), `entries[]`, `pem`, `hr` (simulated live in prototype: resting+3..+20; **in app: from Health Connect**).
- Entry: `{ kind: wake|intention|activity|rest|pem|meds|symptom|vitals|note, name, sub?, time, p?,c?,e?,total? }`.
- Derived: `spentP/C/E` = Σ over activity entries; `spentTotal`; HR state from `hr` vs `ceiling` (over `>`, near `>ceiling−4`, else within); mood/label from `energy`.

## Data & platform mapping
- **Room entities:** `DiaryEntry(id, kind, name, sub, epochMillis, p, c, e, total)`; `DayIntention(dateEpochDay, battery, zone, sleep, startEnergy)`; optional `Vitals(epochMillis, hr, restingHr, steps, sleepMinutes)`. DAO: today's entries; summed effort; **trigger detection = activities within 48h before each PEM entry, ranked by frequency.**
- **Health Connect (read‑only, optional):** `HeartRateRecord`, `RestingHeartRateRecord`, `SleepSessionRecord`, `StepsRecord`. Gate behind a Settings opt‑in; degrade gracefully to manual entry. Pacing ceiling = resting + 15 bpm. **Never request `INTERNET`.**
- **Theme:** encode the tokens above as a Material 3 `ColorScheme` + `Typography` (washi theme); Newsreader/Zen Kaku Gothic New bundled as fonts. Crash mode is a separate dark surface, not the M3 dark scheme.
- **Nav:** a sealed `Screen` state (or Navigation‑Compose); sheets via `ModalBottomSheet`.

## Assets
No raster assets. The lantern is drawn (gradients + shapes). All activity/rest icons are **single‑path line glyphs** — exact `d` strings are in `Akari.dc.html` (`presetData()` / `restData()`); reproduce as Compose `Path`s or vector drawables (1.6px stroke, round caps, `sumi2`; rest icons `sage`). Fonts: Google Fonts (Newsreader, Zen Kaku Gothic New).

## Full app — everything is included
The complete app is designed in one DC: the tab shell (Today · Trends · ＋ · History · Settings), **Trends** (energy‑envelope + trigger detection), **History**, **Settings**, **Health Connect**, and **Symptom / Vitals / Food‑&‑meds** logging via the ＋ sheet. CSV export + JSON backup/restore are represented as Settings actions — wire them to real file I/O in‑app.

## Files
- `Akari.dc.html` — the full interactive prototype, **v2** (all screens + tab shell, state model, lantern, exact icon paths, motion). Open in a browser; read the `class Component` (logic) and `renderVals()` (state → view) for precise behavior.
- `support.js` — runtime the prototype needs to render in a browser. Not part of the design; do not port.
- `compose/Theme.kt` — Kotlin theme starter: every color, type role, shape, dimension, and motion constant transcribed 1:1 from the prototype. Drop into `ui/theme/`.
- `ARCHITECTURE.md` — stack, package layout, Room schema, PacingEngine derivations, Health Connect integration, navigation map, build + CI steps.
- `QA_CHECKLIST.md` — ME/CFS-specific accessibility non-negotiables + per-screen acceptance criteria. Walk it before any release.
- `reference/` — pixel screenshots of the v2 prototype, in flow order: 01 welcome · 02 how-it-helps · 03 morning check-in · 04 home · 05 crash mode · 06 trends · 07 history · 08 history expanded · 09 settings · 10 health connect · 11 first-run home · 12 first-run trends (empty) · 13 first-run history (empty) · 14 the prototype-only first-run toggle.
