package com.akari.app.ui

import com.akari.app.domain.ActivityPreset

/**
 * Static design data transcribed 1:1 from the prototype (presetData /
 * restData / triggerData / symTagList). Icon `d` strings are reproduced as
 * Compose vector paths at render time.
 */
object DesignData {

    val presets: List<ActivityPreset> = listOf(
        ActivityPreset("Shower", "M12 3c3 4 5 6.6 5 9a5 5 0 0 1-10 0c0-2.4 2-5 5-9z", 5, 1, 1),
        ActivityPreset("Cook a meal", "M4 11h16a8 8 0 0 1-16 0zM12 11V4M9 4v3M15 4v3", 4, 3, 1),
        ActivityPreset("Short walk", "M6 6l5 6-5 6M12 6l5 6-5 6", 6, 1, 1),
        ActivityPreset("Phone call", "M6 4h3l2 5-2 1a10 10 0 0 0 5 5l1-2 5 2v3a2 2 0 0 1-2 2A15 15 0 0 1 4 6 2 2 0 0 1 6 4z", 1, 3, 3),
        ActivityPreset("Screen time", "M3 5h18v11H3zM8 20h8M12 16v4", 1, 4, 1),
        ActivityPreset("Errand out", "M6 8h12l-1 12H7L6 8zM9 8a3 3 0 0 1 6 0", 5, 3, 3),
        ActivityPreset("See a friend", "M9 11a3 3 0 1 0 0-6 3 3 0 0 0 0 6zM3 20a6 6 0 0 1 12 0M16 5.2A3 3 0 0 1 18 11M17 14.3A6 6 0 0 1 21 20", 2, 3, 5),
        ActivityPreset("Housework", "M14 4l4 4-7 7-4-4zM11 15l-6 5M14 12l-5 6", 5, 2, 1),
        ActivityPreset("Drive", "M3 13l2-6h14l2 6v5H3zM3 15h18M6.5 18v2M17.5 18v2", 2, 4, 2),
        ActivityPreset("Appointment", "M4 6h16v14H4zM4 10h16M9 4v3M15 4v3M12 13v4M10 15h4", 3, 3, 4),
    )

    data class Rest(val name: String, val iconPath: String, val savasana: Boolean)

    val rests: List<Rest> = listOf(
        Rest("Lie down", "M3 8v10M3 13h18v5M21 18v-3a2 2 0 0 0-2-2h-7v2", false),
        Rest("Savasana", "M12 20c-5-2-7-6-7-10 3 0 5 1 7 4 2-3 4-4 7-4 0 4-2 8-7 10z", true),
        Rest("Nap", "M20 14a8 8 0 1 1-9-11 6 6 0 0 0 9 11z", false),
        Rest("Meditate", "M12 4a2 2 0 1 0 .01 0M8 20l4-8 4 8M5 14l7-2 7 2", false),
        Rest("Quiet sit", "M4 15a8 4 0 0 0 16 0 8 4 0 0 0-16 0zM12 4v7", false),
        Rest("Nothing at all", "M6 12h12", false),
    )

    val symptomTags: List<String> = listOf(
        "Fatigue", "Brain fog", "Sore throat", "Aches", "Dizziness", "Headache", "Unrefreshing sleep",
    )

    data class LogType(val name: String, val sub: String, val iconPath: String, val sheet: Sheet, val tint: Tint)
    enum class Tint { EMBER, SAGE, PLUM, AI, SUMI2, CLAY }

    val logTypes: List<LogType> = listOf(
        LogType("Activity", "one-tap presets", "M13 3L4 14h6l-1 7 9-11h-6l1-7z", Sheet.Activity, Tint.EMBER),
        LogType("Rest", "savasana counts", "M20 14a8 8 0 1 1-9-11 6 6 0 0 0 9 11z", Sheet.Rest, Tint.SAGE),
        LogType("Symptom", "how you feel", "M3 12h4l2 5 3-11 2 6h4", Sheet.Symptom, Tint.PLUM),
        LogType("Vitals", "HR · BP · temp", "M12 21s-7.5-4.9-10-9.2C.3 8.6 1.7 5 5.2 5 7.3 5 8.7 6.2 12 9c3.3-2.8 4.7-4 6.8-4 3.5 0 4.9 3.6 3.2 6.8C19.5 16.1 12 21 12 21Z", Sheet.Vitals, Tint.AI),
        LogType("Food & meds", "a quick note", "M6 3v7a3 3 0 0 0 6 0V3M9 3v18M16 3c-1.5 1-2 3-2 6s.5 4 2 4 2-1 2-4-.5-5-2-6z", Sheet.FoodMeds, Tint.SUMI2),
        // "Flag PEM" uses Sheet.None as a sentinel → the ViewModel flags PEM directly.
        LogType("Flag PEM", "the crash data point", "M5 3v18M5 4h11l-1.5 4L16 12H5", Sheet.None, Tint.CLAY),
    )

    /** Lantern warmth options (must match AkariColors.LanternHues order). */
    val hues: List<Long> = listOf(0xFFEBA950, 0xFFE7B85C, 0xFFE0896B, 0xFFB9C4D0)
}
