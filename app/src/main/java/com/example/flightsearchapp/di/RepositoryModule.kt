package com.example.flightsearchapp.di

import android.content.Context
import com.example.flightsearchapp.data.local.dao.FlightDao
import com.example.flightsearchapp.data.local.database.FlightSearchDatabase
import com.example.flightsearchapp.data.repository.FlightRepository
import com.example.flightsearchapp.data.repository.OfflineFlightRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFlightRepository(
        offlineFlightRepository: OfflineFlightRepository
    ): FlightRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFlightDatabase(@ApplicationContext context: Context): FlightSearchDatabase {
        return FlightSearchDatabase.getDatabase(context)
    }

    @Provides
    fun provideFlightDao(database: FlightSearchDatabase): FlightDao {
        return database.flightDao()
    }
}