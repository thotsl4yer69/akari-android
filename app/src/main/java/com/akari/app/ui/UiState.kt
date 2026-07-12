package com.akari.app.ui

import androidx.compose.ui.graphics.Color
import com.akari.app.domain.DayRecord
import com.akari.app.domain.DiaryEntry
import com.akari.app.domain.SleepQuality
import com.akari.app.domain.Zone

enum class Screen { Onboarding, Morning, Home, Trends, History, Settings, HealthConnect, Crash }

enum class Sheet { None, Log, Activity, Rest, Symptom, Vitals, FoodMeds }

/** The complete, immutable UI state — the single source of truth for the app. */
data class UiState(
    val screen: Screen = Screen.Onboarding,
    val sheet: Sheet = Sheet.None,

    // profile / settings
    val onbStep: Int = 0,
    val name: String = "",
    val restingHr: Int = 67,
    val restingHrText: String = "67",
    val lanternHue: Color = Color(0xFFEBA950),
    val poetic: Boolean = true,

    // morning inputs
    val zone: Zone = Zone.AMBER,
    val battery: Int = 60,
    val sleep: SleepQuality = SleepQuality.POOR,

    // today
    val startBattery: Int = 60,
    val entries: List<DiaryEntry> = emptyList(),
    val pem: Boolean = false,
    val pemTime: String? = null,
    val hr: Int? = null,            // null → no wearable linked
    val hcConnected: Boolean = false,
    val hcPerms: Map<String, Boolean> = mapOf("hr" to true, "resting" to true, "sleep" to true, "steps" to true),

    // trends / history
    val history: List<DayRecord> = emptyList(),
    val allEntries: List<DiaryEntry> = emptyList(),
    val openDay: Int? = null,

    // crash mode done-states
    val crashRest: Boolean = false,
    val crashPem: Boolean = false,
    val crashMeds: Boolean = false,

    // forms
    val symSev: Int = 3,
    val symTags: Set<String> = emptySet(),
    val vHr: String = "", val vSys: String = "", val vDia: String = "", val vTemp: String = "",
    val fFood: String = "", val fMeds: String = "",

    val toast: String? = null,
) {
    val ceiling: Int get() = restingHr + 15
    val energy: Int get() = com.akari.app.domain.PacingEngine.remaining(startBattery, entries)
    val hasToday: Boolean get() = entries.any { it.kind == com.akari.app.domain.EntryKind.INTENTION || it.kind == com.akari.app.domain.EntryKind.WAKE }
}
