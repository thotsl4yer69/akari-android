package com.akari.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PacingEngineTest {

    private fun activity(name: String, at: Long, p: Int, c: Int, e: Int) = DiaryEntry(
        id = at, kind = EntryKind.ACTIVITY, name = name, epochMillis = at,
        timeLabel = "", p = p, c = c, e = e, total = p + c + e,
    )

    private fun pem(at: Long) = DiaryEntry(
        id = at, kind = EntryKind.PEM, name = "PEM flagged", epochMillis = at, timeLabel = "",
    )

    // ---- remaining ----
    @Test fun remaining_subtracts_activity_totals() {
        val entries = listOf(
            activity("Shower", 1, 5, 1, 1),   // 7
            activity("Phone call", 2, 1, 3, 3), // 7
            DiaryEntry(3, EntryKind.REST, "Nap", epochMillis = 3, timeLabel = ""), // no cost
        )
        assertEquals(78 - 14, PacingEngine.remaining(78, entries))
    }

    @Test fun remaining_floors_at_zero() {
        val entries = listOf(activity("Errand", 1, 20, 20, 20)) // 60
        assertEquals(0, PacingEngine.remaining(28, entries))
    }

    @Test fun remaining_ignores_non_activities() {
        val entries = listOf(
            DiaryEntry(1, EntryKind.WAKE, "Woke", epochMillis = 1, timeLabel = ""),
            DiaryEntry(2, EntryKind.PEM, "PEM", epochMillis = 2, timeLabel = ""),
        )
        assertEquals(60, PacingEngine.remaining(60, entries))
    }

    // ---- ceiling ----
    @Test fun ceiling_is_resting_plus_15() {
        assertEquals(82, PacingEngine.ceiling(67))
    }

    // ---- zone ----
    @Test fun zone_thresholds() {
        assertEquals(Zone.GREEN, PacingEngine.zoneOf(70))
        assertEquals(Zone.GREEN, PacingEngine.zoneOf(85))
        assertEquals(Zone.AMBER, PacingEngine.zoneOf(69))
        assertEquals(Zone.AMBER, PacingEngine.zoneOf(40))
        assertEquals(Zone.RED, PacingEngine.zoneOf(39))
        assertEquals(Zone.RED, PacingEngine.zoneOf(0))
    }

    @Test fun zone_preset_batteries() {
        assertEquals(85, Zone.GREEN.presetBattery)
        assertEquals(60, Zone.AMBER.presetBattery)
        assertEquals(28, Zone.RED.presetBattery)
    }

    // ---- HR state (prototype boundary: ceiling-4) ----
    @Test fun hr_state_boundaries() {
        val resting = 67 // ceiling 82
        assertEquals(HrState.ABOVE, PacingEngine.hrState(83, resting))
        assertEquals(HrState.NEAR, PacingEngine.hrState(82, resting))  // == ceiling → near
        assertEquals(HrState.NEAR, PacingEngine.hrState(79, resting))  // ceiling-3
        assertEquals(HrState.WITHIN, PacingEngine.hrState(78, resting)) // ceiling-4 → within
        assertEquals(HrState.WITHIN, PacingEngine.hrState(70, resting))
    }

    // ---- mood ----
    @Test fun mood_bands() {
        assertEquals(MoodBand.BRIGHT, PacingEngine.moodBand(67))
        assertEquals(MoodBand.SOFTENING, PacingEngine.moodBand(66))
        assertEquals(MoodBand.SOFTENING, PacingEngine.moodBand(43))
        assertEquals(MoodBand.LOW, PacingEngine.moodBand(42))
        assertEquals(MoodBand.LOW, PacingEngine.moodBand(21))
        assertEquals(MoodBand.NEARLY_OUT, PacingEngine.moodBand(20))
        assertEquals(MoodBand.NEARLY_OUT, PacingEngine.moodBand(5))
        assertEquals(MoodBand.ALMOST_DARK, PacingEngine.moodBand(4))
        assertEquals(MoodBand.ALMOST_DARK, PacingEngine.moodBand(0))
    }

    @Test fun mood_line_respects_voice() {
        assertEquals("Softening through the day", PacingEngine.moodLine(60, poetic = true))
        assertEquals("Softening", PacingEngine.moodLine(60, poetic = false))
    }

    // ---- split ----
    @Test fun split_sums_each_dimension() {
        val entries = listOf(
            activity("Shower", 1, 5, 1, 1),
            activity("Phone call", 2, 1, 3, 3),
        )
        val s = PacingEngine.split(entries)
        assertEquals(6, s.p); assertEquals(4, s.c); assertEquals(4, s.e); assertEquals(14, s.total)
    }

    // ---- trends ----
    @Test fun bar_level_bands() {
        assertEquals(PacingEngine.BarLevel.GREEN, PacingEngine.barLevel(61))
        assertEquals(PacingEngine.BarLevel.AMBER, PacingEngine.barLevel(60))
        assertEquals(PacingEngine.BarLevel.AMBER, PacingEngine.barLevel(35))
        assertEquals(PacingEngine.BarLevel.CLAY, PacingEngine.barLevel(34))
    }

    @Test fun avg_spent_rounds() {
        val days = listOf(
            DayRecord("Mon", "Jul 7", 70, 22, SleepQuality.POOR, 48, true, 24, 14, 10),
            DayRecord("Sun", "Jul 6", 85, 30, SleepQuality.OKAY, 55, false, 22, 20, 13),
        )
        assertEquals(52, PacingEngine.avgSpentPerDay(days)) // (48+55)/2 = 51.5 → 52
        assertEquals(1, PacingEngine.crashesFlagged(days))
    }

    @Test fun avg_spent_empty_is_zero() {
        assertEquals(0, PacingEngine.avgSpentPerDay(emptyList()))
    }

    // ---- trigger detection ----
    @Test fun triggers_rank_activities_in_48h_before_crash() {
        val hour = 60L * 60 * 1000
        val crash = 100L * hour
        val entries = listOf(
            activity("Errand out", crash - 10 * hour, 5, 3, 3), // in window
            activity("See a friend", crash - 40 * hour, 2, 3, 5), // in window
            activity("Errand out", crash - 20 * hour, 5, 3, 3), // in window → Errand=2
            activity("Old thing", crash - 60 * hour, 1, 1, 1),  // outside 48h
            pem(crash),
            activity("After crash", crash + 5 * hour, 1, 1, 1), // after → excluded
        )
        val triggers = PacingEngine.detectTriggers(entries)
        assertEquals("Errand out", triggers.first().name)
        assertEquals(2, triggers.first().count)
        assertTrue(triggers.any { it.name == "See a friend" && it.count == 1 })
        assertTrue(triggers.none { it.name == "Old thing" })
        assertTrue(triggers.none { it.name == "After crash" })
    }

    @Test fun triggers_empty_without_pem() {
        val entries = listOf(activity("Errand out", 1, 5, 3, 3))
        assertTrue(PacingEngine.detectTriggers(entries).isEmpty())
    }
}
