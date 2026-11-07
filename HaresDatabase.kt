package com.monzer.hares

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SecurityLogEntity::class], version = 1, exportSchema = false)
abstract class HaresDatabase : RoomDatabase() {

    abstract fun securityLogDao(): SecurityLogDao

    companion object {
        @Volatile
        private var INSTANCE: HaresDatabase? = null

        fun getDatabase(context: Context): HaresDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HaresDatabase::class.java,
                    "hares_secure_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
