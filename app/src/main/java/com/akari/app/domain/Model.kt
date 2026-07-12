package com.akari.app.domain

/**
 * Pure domain model — no Android or Compose types. The three effort
 * dimensions carry fixed meaning everywhere (colors are applied in the UI):
 * Physical = clay, Cognitive = indigo (ai), Emotional = plum.
 */
enum class Dimension { PHYSICAL, COGNITIVE, EMOTIONAL }

/** Morning zone. Thresholds and preset batteries live on the enum. */
enum class Zone(val label: String, val presetBattery: Int) {
    GREEN("Steady", 85),
    AMBER("Careful", 60),
    RED("Low", 28);
}

enum class SleepQuality(val label: String) {
    BROKEN("Broken"), POOR("Poor"), OKAY("Okay"), RESTED("Rested");

    /** Past-tense word for the "Woke · slept X" entry sub. */
    val wokeWord: String
        get() = when (this) {
            BROKEN -> "broken"; POOR -> "poorly"; OKAY -> "okay"; RESTED -> "well"
        }
}

/** Heart-rate pace state relative to the ceiling. */
enum class HrState(val tag: String) {
    WITHIN("within pace"), NEAR("near ceiling"), ABOVE("above ceiling");
}

/** Energy band → mood word / poetic line. Boundaries: 66 / 42 / 20 / 4. */
enum class MoodBand { BRIGHT, SOFTENING, LOW, NEARLY_OUT, ALMOST_DARK }

/**
 * Every kind of timeline entry. `wake`/`intention` are day markers; the
 * costed kinds carry p/c/e amounts.
 */
enum class EntryKind {
    WAKE, INTENTION, ACTIVITY, REST, PEM, MEDS, SYMPTOM, VITALS, NOTE;
}

/** A single logged event. Amounts are 0 for non-activity kinds. */
data class DiaryEntry(
    val id: Long,
    val kind: EntryKind,
    val name: String,
    val sub: String? = null,
    val epochMillis: Long,
    val timeLabel: String,
    val p: Int = 0,
    val c: Int = 0,
    val e: Int = 0,
    val total: Int = 0,
)

/** One activity preset (Physical/Cognitive/Emotional split). */
data class ActivityPreset(
    val name: String,
    val iconPath: String,
    val p: Int,
    val c: Int,
    val e: Int,
) {
    val total: Int get() = p + c + e
}

/** A finished day, as shown in History / Trends. */
data class DayRecord(
    val dow: String,
    val date: String,
    val startBattery: Int,
    val remaining: Int,
    val sleep: SleepQuality,
    val spent: Int,
    val pem: Boolean,
    val p: Int,
    val c: Int,
    val e: Int,
)
