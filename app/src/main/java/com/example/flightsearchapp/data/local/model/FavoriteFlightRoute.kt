package com.example.flightsearchapp.data.local.model

data class FavoriteFlightRoute(
    val favorite: Favorite,
    val departureAirport: Airport,
    val destinationAirport: Airport
)