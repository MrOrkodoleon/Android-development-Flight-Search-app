package com.example.flightsearchapp.ui.viewmodel

import com.example.flightsearchapp.data.local.model.Airport
import com.example.flightsearchapp.data.local.model.FavoriteFlightRoute

data class FlightSearchUiState(
    val searchQuery: String = "",
    val searchResults: List<Airport> = emptyList(),
    val selectedDepartureAirport: Airport? = null,
    val destinationSuggestions: List<Airport> = emptyList(),
    val favoriteRoutes: List<FavoriteFlightRoute> = emptyList(),
    val isLoading: Boolean = false,
    val noResultsFound: Boolean = false
)
