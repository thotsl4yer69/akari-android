package com.akari.app.domain

import kotlin.math.roundToInt

/**
 * All pacing derivations — pure functions, no side effects, unit-tested.
 * Where this file and the prototype (Akari.dc.html) disagree, the prototype
 * wins; the HR "near" boundary therefore uses ceiling − 4, not − 5.
 */
object PacingEngine {

    /** remaining = startBattery − Σ activity totals, floored at 0. This IS the lantern. */
    fun remaining(startBattery: Int, entries: List<DiaryEntry>): Int {
        val spent = entries.filter { it.kind == EntryKind.ACTIVITY }.sumOf { it.total }
        return (startBattery - spent).coerceAtLeast(0)
    }

    /** Pacing ceiling in bpm. */
    fun ceiling(restingHr: Int): Int = restingHr + 15

    /** Morning zone from a 0..100 battery. */
    fun zoneOf(battery: Int): Zone = when {
        battery >= 70 -> Zone.GREEN
        battery >= 40 -> Zone.AMBER
        else -> Zone.RED
    }

    /**
     * HR pace tag. over = hr > ceiling; near = hr in (ceiling−4, ceiling];
     * otherwise within pace. Matches the prototype exactly.
     */
    fun hrState(hr: Int, restingHr: Int): HrState {
        val ceil = ceiling(restingHr)
        return when {
            hr > ceil -> HrState.ABOVE
            hr > ceil - 4 -> HrState.NEAR
            else -> HrState.WITHIN
        }
    }

    /** Energy band from remaining light. Boundaries 66 / 42 / 20 / 4. */
    fun moodBand(energy: Int): MoodBand = when {
        energy > 66 -> MoodBand.BRIGHT
        energy > 42 -> MoodBand.SOFTENING
        energy > 20 -> MoodBand.LOW
        energy > 4 -> MoodBand.NEARLY_OUT
        else -> MoodBand.ALMOST_DARK
    }

    /** Plain mood word. */
    fun moodWord(energy: Int): String = when (moodBand(energy)) {
        MoodBand.BRIGHT -> "Bright"
        MoodBand.SOFTENING -> "Softening"
        MoodBand.LOW -> "Low — tend gently"
        MoodBand.NEARLY_OUT -> "Nearly out"
        MoodBand.ALMOST_DARK -> "Please rest"
    }

    /** Home mood line — poetic (italic) or plain, per the Gentle-voice toggle. */
    fun moodLine(energy: Int, poetic: Boolean): String {
        if (!poetic) return moodWord(energy)
        return when (moodBand(energy)) {
            MoodBand.BRIGHT -> "Bright, and yours to spend gently"
            MoodBand.SOFTENING -> "Softening through the day"
            MoodBand.LOW -> "Low — tend it gently"
            MoodBand.NEARLY_OUT -> "Nearly out. Please rest."
            MoodBand.ALMOST_DARK -> "The lantern is almost dark."
        }
    }

    /** Summed effort split over activity entries. */
    data class Split(val p: Int, val c: Int, val e: Int) {
        val total: Int get() = p + c + e
    }

    fun split(entries: List<DiaryEntry>): Split {
        val acts = entries.filter { it.kind == EntryKind.ACTIVITY }
        return Split(acts.sumOf { it.p }, acts.sumOf { it.c }, acts.sumOf { it.e })
    }

    /** Trends bar color band by remaining light: >60 green, >34 amber, else clay. */
    enum class BarLevel { GREEN, AMBER, CLAY }

    fun barLevel(remaining: Int): BarLevel = when {
        remaining > 60 -> BarLevel.GREEN
        remaining > 34 -> BarLevel.AMBER
        else -> BarLevel.CLAY
    }

    /** Bar opacity by level: 0.45 + light/100 * 0.55. */
    fun barOpacity(remaining: Int): Float = (0.45f + remaining / 100f * 0.55f)

    fun crashesFlagged(days: List<DayRecord>): Int = days.count { it.pem }

    fun avgSpentPerDay(days: List<DayRecord>): Int =
        if (days.isEmpty()) 0 else (days.sumOf { it.spent }.toDouble() / days.size).roundToInt()

    fun avgLight(remainings: List<Int>): Int =
        if (remainings.isEmpty()) 0 else (remainings.sum().toDouble() / remainings.size).roundToInt()

    /** A ranked possible-trigger row. */
    data class Trigger(val name: String, val count: Int)

    /**
     * Trigger detection: activities logged within the 48h before each PEM
     * entry, ranked by frequency (most frequent first). Patterns, not proof.
     */
    fun detectTriggers(entries: List<DiaryEntry>, limit: Int = 5): List<Trigger> {
        val window = 48L * 60 * 60 * 1000
        val pemTimes = entries.filter { it.kind == EntryKind.PEM }.map { it.epochMillis }
        if (pemTimes.isEmpty()) return emptyList()
        val counts = LinkedHashMap<String, Int>()
        for (act in entries.filter { it.kind == EntryKind.ACTIVITY }) {
            val precedesACrash = pemTimes.any { pem ->
                act.epochMillis in (pem - window)..pem
            }
            if (precedesACrash) counts[act.name] = (counts[act.name] ?: 0) + 1
        }
        return counts.entries
            .map { Trigger(it.key, it.value) }
            .sortedByDescending { it.count }
            .take(limit)
    }
}
