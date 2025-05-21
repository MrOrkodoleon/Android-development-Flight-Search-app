package com.example.flightsearchapp.data.repository

import com.example.flightsearchapp.data.local.model.Airport
import com.example.flightsearchapp.data.local.model.Favorite
import kotlinx.coroutines.flow.Flow

interface FlightRepository {
    fun getAirportsByQuery(query: String): Flow<List<Airport>>
    fun getAirportByIataCode(iataCode: String): Flow<Airport?>
    fun getAllAirports(): Flow<List<Airport>>

    fun getAllFavorites(): Flow<List<Favorite>>
    suspend fun addFavorite(favorite: Favorite)
    suspend fun removeFavorite(departureCode: String, destinationCode: String)
    fun getFavoriteStatus(departureCode: String, destinationCode: String): Flow<Favorite?>
}