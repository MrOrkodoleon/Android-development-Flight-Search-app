package com.example.flightsearchapp.data.repository

import com.example.flightsearchapp.data.local.dao.FlightDao
import com.example.flightsearchapp.data.local.model.Airport
import com.example.flightsearchapp.data.local.model.Favorite
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFlightRepository @Inject constructor(private val flightDao: FlightDao) :
    FlightRepository {

    override fun getAirportsByQuery(query: String): Flow<List<Airport>> =
        flightDao.getAirportsByQuery(query)

    override fun getAirportByIataCode(iataCode: String): Flow<Airport?> =
        flightDao.getAirportByIataCode(iataCode)

    override fun getAllAirports(): Flow<List<Airport>> =
        flightDao.getAllAirports()

    override fun getAllFavorites(): Flow<List<Favorite>> =
        flightDao.getAllFavorites()

    override suspend fun addFavorite(favorite: Favorite) {
        flightDao.insertFavorite(favorite)
    }

    override suspend fun removeFavorite(departureCode: String, destinationCode: String) {
        flightDao.deleteFavorite(departureCode, destinationCode)
    }

    override fun getFavoriteStatus(
        departureCode: String,
        destinationCode: String
    ): Flow<Favorite?> =
        flightDao.getFavorite(departureCode, destinationCode)
}