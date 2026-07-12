package com.akari.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A day's morning intention. Keyed by the diary day (epoch-day; the day
 * rolls at 04:00 — see DiaryDate). `pem` is set when a crash is flagged.
 */
@Entity(tableName = "days")
data class DayEntity(
    @PrimaryKey val epochDay: Long,
    val startBattery: Int,
    val sleep: String,   // SleepQuality.name
    val zone: String,    // Zone.name
    val pem: Boolean = false,
)

/** Every logged event. Non-activity kinds carry 0 amounts. */
@Entity(
    tableName = "entries",
    indices = [Index("epochDay"), Index("epochMillis")],
)
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val kind: String,    // EntryKind.name
    val name: String,
    val sub: String?,
    val epochMillis: Long,
    val timeLabel: String,
    val p: Int = 0,
    val c: Int = 0,
    val e: Int = 0,
    val total: Int = 0,
)
