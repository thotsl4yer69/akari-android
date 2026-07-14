package com.akari.app.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * Local, dependency-free serialization for the "your data stays on this phone"
 * export/backup/restore. CSV is the human-readable doctor export (one row per
 * logged event); JSON is the full-fidelity backup that round-trips every day
 * summary and entry. Uses only the platform's org.json — no network, no
 * third-party libraries.
 */
object DataExport {

    const val VERSION = 1

    data class Backup(val days: List<DayEntity>, val entries: List<EntryEntity>)

    // ---------------------------------------------------------------- CSV ---
    /** Doctor-facing export: one row per entry, newest first, with the day. */
    fun toCsv(days: List<DayEntity>, entries: List<EntryEntity>): String {
        val sb = StringBuilder()
        sb.append("date,time,kind,name,detail,physical,cognitive,emotional,total\n")
        entries.sortedWith(compareByDescending<EntryEntity> { it.epochMillis }.thenByDescending { it.id })
            .forEach { e ->
                val date = LocalDate.ofEpochDay(e.epochDay).toString()
                sb.append(
                    listOf(
                        date, e.timeLabel, e.kind, e.name, e.sub ?: "",
                        e.p.toString(), e.c.toString(), e.e.toString(), e.total.toString(),
                    ).joinToString(",") { csvCell(it) },
                )
                sb.append("\n")
            }
        return sb.toString()
    }

    private fun csvCell(raw: String): String =
        if (raw.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + raw.replace("\"", "\"\"") + "\""
        } else {
            raw
        }

    // --------------------------------------------------------------- JSON ---
    fun toJson(days: List<DayEntity>, entries: List<EntryEntity>): String {
        val root = JSONObject()
        root.put("version", VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        val dayArr = JSONArray()
        days.forEach { d ->
            dayArr.put(
                JSONObject()
                    .put("epochDay", d.epochDay)
                    .put("startBattery", d.startBattery)
                    .put("sleep", d.sleep)
                    .put("zone", d.zone)
                    .put("pem", d.pem),
            )
        }
        root.put("days", dayArr)

        val entryArr = JSONArray()
        entries.forEach { e ->
            entryArr.put(
                JSONObject()
                    .put("id", e.id)
                    .put("epochDay", e.epochDay)
                    .put("kind", e.kind)
                    .put("name", e.name)
                    .put("sub", e.sub ?: JSONObject.NULL)
                    .put("epochMillis", e.epochMillis)
                    .put("timeLabel", e.timeLabel)
                    .put("p", e.p).put("c", e.c).put("e", e.e).put("total", e.total),
            )
        }
        root.put("entries", entryArr)
        return root.toString(2)
    }

    /** Parse a backup file. Throws on malformed input — callers catch. */
    fun fromJson(text: String): Backup {
        val root = JSONObject(text)
        val dayArr = root.getJSONArray("days")
        val days = ArrayList<DayEntity>(dayArr.length())
        for (i in 0 until dayArr.length()) {
            val o = dayArr.getJSONObject(i)
            days.add(
                DayEntity(
                    epochDay = o.getLong("epochDay"),
                    startBattery = o.getInt("startBattery"),
                    sleep = o.getString("sleep"),
                    zone = o.getString("zone"),
                    pem = o.getBoolean("pem"),
                ),
            )
        }
        val entryArr = root.getJSONArray("entries")
        val entries = ArrayList<EntryEntity>(entryArr.length())
        for (i in 0 until entryArr.length()) {
            val o = entryArr.getJSONObject(i)
            entries.add(
                EntryEntity(
                    id = o.optLong("id", 0L),
                    epochDay = o.getLong("epochDay"),
                    kind = o.getString("kind"),
                    name = o.getString("name"),
                    sub = if (o.isNull("sub")) null else o.getString("sub"),
                    epochMillis = o.getLong("epochMillis"),
                    timeLabel = o.getString("timeLabel"),
                    p = o.optInt("p", 0), c = o.optInt("c", 0),
                    e = o.optInt("e", 0), total = o.optInt("total", 0),
                ),
            )
        }
        return Backup(days, entries)
    }
}
