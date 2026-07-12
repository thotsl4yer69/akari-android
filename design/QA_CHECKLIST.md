# Akari — QA & accessibility checklist

Akari's users have ME/CFS: low energy, brain fog, light/motion sensitivity, often phone-in-bed one-handed use. Accessibility here is not compliance — it is the product. Test everything below on a real device, in bed posture, at 200% font scale, before calling a build done.

## Non-negotiables (fail the build if any fail)
- [ ] Every interactive target ≥48dp; crash-mode tiles ≥128dp tall.
- [ ] Full app usable one-handed from the bottom half of the screen.
- [ ] Font scale 200%: no clipped text, no overlapping layout, buttons grow.
- [ ] TalkBack: every control has a meaningful label; lantern announces "Lantern — {n} of {start} light left"; zone cards announce zone + meaning; HR chip announces value + pace tag; history rows announce day summary + expanded state.
- [ ] `Settings.Global.ANIMATOR_DURATION_SCALE = 0` (or reduce-motion): all softIn/rise/ember animations drop to instant fades. Nothing ever flashes.
- [ ] No pure white, no pure black, no saturated red anywhere (clay is the max alarm level). No sound. No vibration except a single soft tick on crash-mode actions (optional, off by default).
- [ ] Contrast: body inks (sumi, sumi2) on washi/card ≥4.5:1. sumi3 is decorative/overline only — never for essential info without a second cue.
- [ ] Zero data leaves the device; no INTERNET permission in the merged manifest (`aapt dump permissions`).
- [ ] Kill + relaunch mid-flow: state restores (morning done, spends, crash flags, hue, name).
- [ ] New day after 4am → morning check-in appears; yesterday lands in History.
- [ ] Fresh install: Home shows "Nothing spent yet", Trends shows the ghost-bar card, History shows the unlit lantern — no blank screens, no zero-filled charts, and no "First-run state" toggle anywhere in release builds.

## Per-screen acceptance
**Onboarding** — 3 steps; dots animate; Skip available on steps 1–2 only; CTA on step 3 reads "Light your first day"; name + resting HR optional (defaults: no name, 67).
**Morning** — zone cards Steady/Careful/Low select with soft glow; slider clamps 0/100 with thumb fully inside track; battery figure is serif; CTA lights the lantern and lands on Home.
**Home** — lantern glow opacity tracks remaining light; greeting uses name when set; poetic line changes with energy band and disappears when Gentle voice is off; spent-today bar splits phys/cog/emo (clay/ai/plum); HR chip only when Health Connect connected, otherwise dashed "No wearable linked" row linking to HC setup; with nothing logged, the spend bar + legend give way to the quiet "Nothing spent yet" line (card stays); moon button enters crash mode.
**Log sheet** — activity tiles cost visible energy; saving decrements the lantern immediately with a calm toast ("logged, quietly"); symptom severity 1–5; sub-sheets have a back arrow.
**Crash mode** — near-black, dim ember pulsing at 5s; three huge tiles; done labels gain a quiet ✓ ("Resting ✓"); fourth tile returns to the day; entering flags nothing by itself — only explicit taps log.
**Trends** — 14 bars colored by level; crash-day dot under the right bars + "· crash day" legend; stats: crashes flagged, avg spent/day; with <3 days of data, everything is replaced by the single "envelope needs a few days" ghost-bar card — never an empty chart.
**History** — mini-lantern glow matches that day's remaining light; PEM badge on crash days; tap expands inline detail (spend split bar + Phys/Cog/Emo figures, 300ms rise); tap again collapses; open row gets the stronger border; with no completed days, the unlit-lantern "No days here yet" state shows — never a bare list.
**Settings** — lantern-warmth swatches recolor EVERY lantern in the app instantly (home, onboarding, history minis); Gentle voice toggle; Health Connect row shows connected state in sage; CSV export + backup/restore stubs. (The dashed "Preview · prototype only" card is a design-review affordance — it must NOT ship.)
**Health Connect** — read-only framing ("Akari can read…"); per-permission toggles; disconnect returns Home to the no-wearable row.

## Copy rules (enforce in review)
Sentence case everywhere; no exclamation marks; no streaks, scores, or "goals"; never praise doing more; a low day is "not a failure" — the app's voice stays this gentle in every new string.
