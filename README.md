# Akari (明かり)

**A private, local-first Android energy diary for pacing and PEM tracking.**

[![Status](https://img.shields.io/badge/status-application%20prototype-blue)](PROJECT_STATUS.md)
![Android](https://img.shields.io/badge/Android-Kotlin%20%2B%20Compose-green)
![Privacy](https://img.shields.io/badge/data-local--only-blue)

> **Maturity: Application prototype.** Akari is implemented as a native Android application with automated build/test work, but it is not a medical device and should not be presented as diagnosis or treatment software. See [PROJECT_STATUS.md](PROJECT_STATUS.md).

## Product idea

Akari is an energy diary designed around a simple visual metaphor: a paper lantern dims as the day's available energy is spent. The application supports pacing and post-exertional-malaise (PEM) journaling without requiring an account or cloud backend.

Privacy is an architectural choice rather than a marketing afterthought:

- no user account;
- no required cloud service;
- no Android `INTERNET` permission in the core application;
- OS backup disabled for app data;
- explicit in-app JSON export as the user-controlled migration path;
- optional read-only Health Connect integration rather than automatic remote synchronisation.

## Engineering stack

- Kotlin 2;
- Jetpack Compose;
- Room + DataStore persistence;
- optional read-only Health Connect integration;
- single `:app` Android module;
- GitHub Actions build/test workflow;
- emulator smoke testing;
- screenshot/reference-driven UI QA;
- explicit large-font accessibility review.

## Repository layout

```text
app/
  Native Android application: Compose UI, Room/DataStore and integrations.

design/
  Product/design source of truth: interactive prototype, architecture,
  QA checklist, theme starter and reference screenshots.

.github/workflows/android.yml
  CI build, unit-test and emulator-smoke workflow.
```

## Build and test

```bash
./gradlew assembleDebug testDebugUnitTest
```

The debug APK is generated under:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Install with `adb` or sideload it onto a suitable test device. Health Connect behaviour should be tested on a compatible real device/environment rather than inferred from compile success alone.

## QA philosophy

Fresh installs begin with an empty user state. Before sharing a build, work through `design/QA_CHECKLIST.md` on a real device, including the documented **200% font-scale** pass.

For this project, accessibility is part of functional correctness: an energy-management interface that becomes difficult to use under fatigue has failed its own product requirement.

## Evidence boundary

Akari is self-management/journaling software. It does not diagnose ME/CFS, calculate medically validated energy limits, prescribe activity, or replace clinical advice.

Automated builds and emulator smoke tests prove useful software-engineering properties; they do not turn the application into a clinically validated product.

## Development provenance

Akari is an authored MAZLABZ application developed with AI-assisted engineering workflows. AI has supported implementation, research, refactoring and testing; product definition, privacy decisions, integration, QA criteria and verification remain the project owner's responsibility.

## Portfolio significance

Akari demonstrates a different side of the same systems-integration practice as the hardware projects:

**native Android · local persistence · privacy-by-design · CI · emulator QA · accessibility-minded product engineering**
