package com.example.containmentzonealertingapplication.roomdatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.Executors

@Database(entities = [VisitedLocations::class], version = 1, exportSchema = false)
abstract class VisitedLocationsDatabase : RoomDatabase() {
    abstract fun visitedLocationsDao(): VisitedLocationsDao?

    companion object {
        @Volatile
        private var INSTANCE: VisitedLocationsDatabase? = null

        // room queries need to be done on separate threads
        private const val NUMBER_OF_THREADS = 3
        val databaseWriteExecutor = Executors.newFixedThreadPool(
            NUMBER_OF_THREADS
        )

        fun getDatabase(context: Context): VisitedLocationsDatabase? {
            /*
        thread safe singleton design to get database INSTANCE
         */
            if (INSTANCE == null) {
                synchronized(VisitedLocations::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            VisitedLocationsDatabase::class.java, "word_database"
                        )
                            .build()
                    }
                }
            }
            return INSTANCE
        }
    }
}