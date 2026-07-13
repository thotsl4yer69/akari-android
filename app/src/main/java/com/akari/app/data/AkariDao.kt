package com.akari.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AkariDao {

    // ---- days ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDay(day: DayEntity)

    @Query("SELECT * FROM days WHERE epochDay = :epochDay LIMIT 1")
    suspend fun dayOnce(epochDay: Long): DayEntity?

    @Query("SELECT * FROM days WHERE epochDay = :epochDay LIMIT 1")
    fun dayFlow(epochDay: Long): Flow<DayEntity?>

    @Query("SELECT * FROM days ORDER BY epochDay DESC")
    fun allDays(): Flow<List<DayEntity>>

    @Query("UPDATE days SET pem = :pem WHERE epochDay = :epochDay")
    suspend fun setDayPem(epochDay: Long, pem: Boolean)

    // ---- entries ----
    @Insert
    suspend fun insertEntry(entry: EntryEntity): Long

    @Update
    suspend fun updateEntry(entry: EntryEntity)

    @Query("SELECT * FROM entries WHERE epochDay = :epochDay ORDER BY epochMillis ASC, id ASC")
    fun entriesForDay(epochDay: Long): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries ORDER BY epochMillis ASC, id ASC")
    fun allEntries(): Flow<List<EntryEntity>>

    // ---- data management ----
    @Query("DELETE FROM entries")
    suspend fun clearEntries()

    @Query("DELETE FROM days")
    suspend fun clearDays()

    /** Atomically removes every diary entry and day summary. */
    @Transaction
    suspend fun clearAll() {
        clearEntries()
        clearDays()
    }
}
