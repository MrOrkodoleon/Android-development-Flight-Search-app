package com.example.flightsearchapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightsearchapp.data.local.model.Airport
import com.example.flightsearchapp.data.local.model.Favorite
import com.example.flightsearchapp.data.local.model.FavoriteFlightRoute
import com.example.flightsearchapp.data.repository.FlightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class FlightSearchViewModel @Inject constructor(
    private val flightRepository: FlightRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlightSearchUiState())
    val uiState: StateFlow<FlightSearchUiState> = _uiState.asStateFlow()

    private val _searchQueryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            _searchQueryFlow
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        loadFavoriteRoutes()
                        flowOf(emptyList())
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = true,
                                noResultsFound = false,
                                selectedDepartureAirport = null,
                                destinationSuggestions = emptyList()
                            )
                        }
                        flightRepository.getAirportsByQuery(query)
                    }
                }
                .catch { throwable ->
                    _uiState.update { it.copy(isLoading = false, noResultsFound = true) }
                }
                .collect { airports ->
                    _uiState.update {
                        it.copy(
                            searchResults = airports,
                            isLoading = false,
                            noResultsFound = airports.isEmpty() && _searchQueryFlow.value.isNotBlank()
                        )
                    }
                    if (_searchQueryFlow.value.isBlank() && airports.isEmpty()) {
                        loadFavoriteRoutes()
                    }
                }
        }

        loadFavoriteRoutes()
    }

    private fun loadFavoriteRoutes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            flightRepository.getAllFavorites()
                .flatMapLatest { favorites ->
                    // For each favorite, fetch the airport details
                    val detailedFavoritesFlows = favorites.map { fav ->
                        combine(
                            flightRepository.getAirportByIataCode(fav.departureCode),
                            flightRepository.getAirportByIataCode(fav.destinationCode)
                        ) { dep, dest ->
                            if (dep != null && dest != null) {
                                FavoriteFlightRoute(fav, dep, dest)
                            } else {
                                null // Handle case where airport details might be missing
                            }
                        }
                    }
                    // Combine all individual favorite flows into a list of FavoriteFlightRoute
                    if (detailedFavoritesFlows.isEmpty()) {
                        flowOf(emptyList<FavoriteFlightRoute>())
                    } else {
                        combine(detailedFavoritesFlows) { routes ->
                            routes.filterNotNull() // Filter out any nulls if airport data was missing
                        }
                    }
                }
                .catch { throwable ->
                    _uiState.update { it.copy(isLoading = false) }
                }
                .collect { favoriteRoutes ->
                    _uiState.update {
                        it.copy(
                            favoriteRoutes = favoriteRoutes,
                            isLoading = false
                        )
                    }
                }
        }
    }


    fun onQueryChanged(query: String) {
        _searchQueryFlow.value = query // Update the flow that triggers search
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            // If query is cleared, also clear selected airport and suggestions
            _uiState.update {
                it.copy(
                    selectedDepartureAirport = null,
                    destinationSuggestions = emptyList(),
                    searchResults = emptyList()
                )
            }
            loadFavoriteRoutes() // Reload favorites
        }
    }

    fun onAirportSelected(airport: Airport) {
        _uiState.update {
            it.copy(
                selectedDepartureAirport = airport,
                searchQuery = airport.name,
                searchResults = emptyList()
            )
        }
        _searchQueryFlow.value = airport.name

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            flightRepository.getAllAirports() // Get all airports
                .map { allAirports ->
                    allAirports.filter { it.iataCode != airport.iataCode } // Filter out the selected one
                }
                .catch { throwable ->
                    _uiState.update { it.copy(isLoading = false) }
                }
                .collect { suggestions ->
                    _uiState.update {
                        it.copy(
                            destinationSuggestions = suggestions,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onToggleFavorite(departureAirport: Airport, destinationAirport: Airport) {
        viewModelScope.launch {
            val isCurrentlyFavorite = flightRepository.getFavoriteStatus(
                departureAirport.iataCode,
                destinationAirport.iataCode
            ).first() != null // .first() to get current status

            if (isCurrentlyFavorite) {
                flightRepository.removeFavorite(
                    departureAirport.iataCode,
                    destinationAirport.iataCode
                )
            } else {
                flightRepository.addFavorite(
                    Favorite(
                        departureCode = departureAirport.iataCode,
                        destinationCode = destinationAirport.iataCode
                    )
                )
            }
            if (_uiState.value.searchQuery.isBlank() && _uiState.value.selectedDepartureAirport == null) {
                loadFavoriteRoutes()
            }
        }
    }

    fun getFavoriteStatusFlow(departureCode: String, destinationCode: String): Flow<Boolean> {
        return flightRepository.getFavoriteStatus(departureCode, destinationCode)
            .map { it != null } // True if favorite exists, false otherwise
    }
}