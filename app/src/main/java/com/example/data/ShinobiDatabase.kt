package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        HackathonEntry::class,
        Habit::class,
        HabitLog::class,
        FocusSession::class,
        DsaProblem::class,
        Project::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ShinobiDatabase : RoomDatabase() {

    abstract fun hackathonDao(): HackathonDao
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun dsaProblemDao(): DsaProblemDao
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: ShinobiDatabase? = null

        fun getInstance(context: Context): ShinobiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShinobiDatabase::class.java,
                    "shinobi_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
