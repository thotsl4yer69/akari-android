package com.akari.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DayEntity::class, EntryEntity::class], version = 1, exportSchema = false)
abstract class AkariDb : RoomDatabase() {
    abstract fun dao(): AkariDao

    companion object {
        @Volatile private var INSTANCE: AkariDb? = null

        fun get(context: Context): AkariDb = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext, AkariDb::class.java, "akari.db",
            ).build().also { INSTANCE = it }
        }
    }
}
