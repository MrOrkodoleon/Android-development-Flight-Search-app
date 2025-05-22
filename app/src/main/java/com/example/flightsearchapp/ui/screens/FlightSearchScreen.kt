package com.example.flightsearchapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flightsearchapp.R
import com.example.flightsearchapp.ui.composables.AirportSearchResultsList
import com.example.flightsearchapp.ui.theme.FlightSearchAppTheme
import com.example.flightsearchapp.ui.viewmodel.FlightSearchViewModel
import com.example.flightsearchapp.ui.composables.SearchBar
import com.example.flightsearchapp.ui.composables.FlightList
import com.example.flightsearchapp.ui.composables.FavoriteRoutesList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchScreen(
    viewModel: FlightSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name_flight_search)) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onQueryChanged(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading && uiState.searchResults.isEmpty() && uiState.destinationSuggestions.isEmpty() && uiState.favoriteRoutes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when {
                    uiState.selectedDepartureAirport != null -> {
                        Text(
                            text = stringResource(
                                R.string.flights_from_airport,
                                uiState.selectedDepartureAirport!!.iataCode
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (uiState.destinationSuggestions.isNotEmpty()) {
                            FlightList(
                                departureAirport = uiState.selectedDepartureAirport!!,
                                destinationAirports = uiState.destinationSuggestions,
                                onToggleFavorite = { dep, dest ->
                                    viewModel.onToggleFavorite(
                                        dep,
                                        dest
                                    )
                                },
                                getFavoriteStatusFlow = { depCode, destCode ->
                                    viewModel.getFavoriteStatusFlow(
                                        depCode,
                                        destCode
                                    )
                                }
                            )
                        } else if (!uiState.isLoading) {
                            Text(stringResource(R.string.no_destinations_found))
                        }
                    }

                    uiState.searchQuery.isNotBlank() && uiState.searchResults.isNotEmpty() -> {
                        AirportSearchResultsList(
                            airports = uiState.searchResults,
                            onAirportClick = { airport ->
                                viewModel.onAirportSelected(airport)
                                keyboardController?.hide()
                            }
                        )
                    }

                    uiState.searchQuery.isNotBlank() && uiState.noResultsFound && !uiState.isLoading -> {
                        Text(
                            stringResource(
                                R.string.no_airports_found_for_query,
                                uiState.searchQuery
                            )
                        )
                    }

                    uiState.searchQuery.isBlank() && uiState.favoriteRoutes.isNotEmpty() -> {
                        Text(
                            text = stringResource(R.string.favorite_routes),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        FavoriteRoutesList(
                            favoriteRoutes = uiState.favoriteRoutes,
                            onToggleFavorite = { dep, dest ->
                                viewModel.onToggleFavorite(
                                    dep,
                                    dest
                                )
                            }
                        )
                    }

                    uiState.searchQuery.isBlank() && uiState.favoriteRoutes.isEmpty() && !uiState.isLoading -> {
                        Text(stringResource(R.string.start_searching_or_add_favorites))
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun FlightSearchScreenPreview() {
    FlightSearchAppTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            SearchBar(query = "London", onQueryChange = {})
            Spacer(modifier = Modifier.height(16.dp))
            Text("Search results or favorites would appear here.")
        }
    }
}