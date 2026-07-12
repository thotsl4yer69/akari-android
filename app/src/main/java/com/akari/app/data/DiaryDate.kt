package com.akari.app.data

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * The diary day rolls at 04:00 — anything logged before 4am still belongs to
 * "yesterday". A new day after 4am triggers the morning check-in and lands
 * yesterday in History.
 */
object DiaryDate {
    private const val ROLL_HOUR = 4

    fun current(zone: ZoneId = ZoneId.systemDefault(), now: LocalDateTime = LocalDateTime.now(zone)): LocalDate =
        if (now.hour < ROLL_HOUR) now.toLocalDate().minusDays(1) else now.toLocalDate()

    fun currentEpochDay(zone: ZoneId = ZoneId.systemDefault()): Long = current(zone).toEpochDay()

    private val time = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    private val longDate = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US)
    private val dow = DateTimeFormatter.ofPattern("EEE", Locale.US)
    private val shortDate = DateTimeFormatter.ofPattern("MMM d", Locale.US)

    fun timeLabel(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zone).format(time)

    fun longLabel(date: LocalDate): String = date.format(longDate)

    fun dowLabel(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(dow)

    fun shortLabel(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(shortDate)
}
