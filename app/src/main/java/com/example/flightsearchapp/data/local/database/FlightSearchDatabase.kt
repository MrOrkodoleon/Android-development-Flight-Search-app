package com.example.flightsearchapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.flightsearchapp.data.local.dao.FlightDao
import com.example.flightsearchapp.data.local.model.Airport
import com.example.flightsearchapp.data.local.model.Favorite

@Database(entities = [Airport::class, Favorite::class], version = 1, exportSchema = false)
abstract class FlightSearchDatabase : RoomDatabase() {

    abstract fun flightDao(): FlightDao

    companion object {
        @Volatile
        private var Instance: FlightSearchDatabase? = null

        fun getDatabase(context: Context): FlightSearchDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    FlightSearchDatabase::class.java,
                    "flight_search.db"
                )
                    .createFromAsset("database/flight_search.db") // Pre-populate
                    // .fallbackToDestructiveMigration() // For simplicity in dev; use proper migrations in prod
                    .build()
                    .also { Instance = it }
            }
        }
    }
}