package com.akari.app.data

import com.akari.app.domain.DiaryEntry
import com.akari.app.domain.DayRecord
import com.akari.app.domain.EntryKind
import com.akari.app.domain.PacingEngine
import com.akari.app.domain.SleepQuality
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** Bridges Room rows to the pure domain model used by the UI. */
class DiaryRepository(private val dao: AkariDao) {

    // ---- today ----
    fun day(epochDay: Long): Flow<DayEntity?> = dao.dayFlow(epochDay)
    fun entriesForDay(epochDay: Long): Flow<List<DiaryEntry>> =
        dao.entriesForDay(epochDay).map { rows -> rows.map { it.toDomain() } }

    suspend fun dayOnce(epochDay: Long): DayEntity? = dao.dayOnce(epochDay)

    suspend fun startDay(epochDay: Long, startBattery: Int, sleep: SleepQuality, zone: com.akari.app.domain.Zone) {
        dao.upsertDay(DayEntity(epochDay, startBattery, sleep.name, zone.name, pem = false))
    }

    suspend fun addEntry(entry: DiaryEntry, epochDay: Long): Long =
        dao.insertEntry(entry.toEntity(epochDay))

    suspend fun setPem(epochDay: Long, pem: Boolean) = dao.setDayPem(epochDay, pem)

    // ---- history (finished days only) ----
    fun history(todayEpochDay: Long): Flow<List<DayRecord>> =
        combine(dao.allDays(), dao.allEntries()) { days, entries ->
            val byDay = entries.groupBy { it.epochDay }
            days.filter { it.epochDay < todayEpochDay }
                .sortedByDescending { it.epochDay }
                .map { d ->
                    val de = byDay[d.epochDay].orEmpty().map { it.toDomain() }
                    val split = PacingEngine.split(de)
                    DayRecord(
                        dow = DiaryDate.dowLabel(d.epochDay),
                        date = DiaryDate.shortLabel(d.epochDay),
                        startBattery = d.startBattery,
                        remaining = PacingEngine.remaining(d.startBattery, de),
                        sleep = runCatching { SleepQuality.valueOf(d.sleep) }.getOrDefault(SleepQuality.OKAY),
                        spent = split.total,
                        pem = d.pem,
                        p = split.p, c = split.c, e = split.e,
                    )
                }
        }

    /** All entries across days — for 48h trigger detection. */
    fun allEntries(): Flow<List<DiaryEntry>> = dao.allEntries().map { rows -> rows.map { it.toDomain() } }

    // ---- data management ----
    /** Atomically removes every diary entry and day summary. */
    suspend fun wipeAll() = dao.clearAll()

    // ---- export / backup / restore ----
    suspend fun allDaysOnce(): List<DayEntity> = dao.allDaysList()
    suspend fun allEntriesOnce(): List<EntryEntity> = dao.allEntriesList()

    /** Replaces the entire diary with a restored backup, atomically. */
    suspend fun replaceAll(days: List<DayEntity>, entries: List<EntryEntity>) =
        dao.replaceAll(days, entries)
}

private fun EntryEntity.toDomain() = DiaryEntry(
    id = id,
    kind = runCatching { EntryKind.valueOf(kind) }.getOrDefault(EntryKind.NOTE),
    name = name, sub = sub, epochMillis = epochMillis, timeLabel = timeLabel,
    p = p, c = c, e = e, total = total,
)

private fun DiaryEntry.toEntity(epochDay: Long) = EntryEntity(
    id = if (id > 0) id else 0,
    epochDay = epochDay, kind = kind.name, name = name, sub = sub,
    epochMillis = epochMillis, timeLabel = timeLabel, p = p, c = c, e = e, total = total,
)
