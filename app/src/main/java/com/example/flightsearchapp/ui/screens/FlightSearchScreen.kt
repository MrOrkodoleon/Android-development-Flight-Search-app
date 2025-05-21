package com.example.flightsearchapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flightsearchapp.R
import com.example.flightsearchapp.data.local.model.Airport
import com.example.flightsearchapp.ui.theme.FlightSearchAppTheme
import com.example.flightsearchapp.data.local.model.FavoriteFlightRoute
import com.example.flightsearchapp.ui.viewmodel.FlightSearchViewModel
import kotlin.collections.isNotEmpty
import kotlinx.coroutines.flow.Flow
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.HorizontalDivider

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
                        // Show destination suggestions for the selected airport
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

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(stringResource(R.string.search_airports_label)) },
        leadingIcon = {
            Icon(
                Icons.Filled.Search,
                contentDescription = stringResource(R.string.search_icon_desc)
            )
        },
        singleLine = true,
        modifier = modifier
    )
}

@Composable
fun AirportSearchResultsList(
    airports: List<Airport>,
    onAirportClick: (Airport) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(airports, key = { it.id }) { airport ->
            AirportListItem(airport = airport, onClick = { onAirportClick(airport) })
            HorizontalDivider()
        }
    }
}

@Composable
fun AirportListItem(
    airport: Airport,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = airport.iataCode,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.width(60.dp) // Fixed width for IATA code for alignment
        )
        Text(
            text = airport.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun FlightList(
    departureAirport: Airport,
    destinationAirports: List<Airport>,
    onToggleFavorite: (departure: Airport, destination: Airport) -> Unit,
    getFavoriteStatusFlow: (departureCode: String, destinationCode: String) -> Flow<Boolean>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(
            destinationAirports,
            key = { "route-${departureAirport.id}-${it.id}" }) { destinationAirport ->
            val isFavorite by getFavoriteStatusFlow(
                departureAirport.iataCode,
                destinationAirport.iataCode
            )
                .collectAsStateWithLifecycle(initialValue = false)

            FlightRouteItem(
                departureAirport = departureAirport,
                destinationAirport = destinationAirport,
                isFavorite = isFavorite,
                onToggleFavorite = { onToggleFavorite(departureAirport, destinationAirport) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun FavoriteRoutesList(
    favoriteRoutes: List<FavoriteFlightRoute>,
    onToggleFavorite: (departure: Airport, destination: Airport) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(favoriteRoutes, key = { "fav-${it.favorite.id}" }) { route ->
            FlightRouteItem(
                departureAirport = route.departureAirport,
                destinationAirport = route.destinationAirport,
                isFavorite = true,
                onToggleFavorite = {
                    onToggleFavorite(
                        route.departureAirport,
                        route.destinationAirport
                    )
                }
            )
            HorizontalDivider()
        }
    }
}


@Composable
fun FlightRouteItem(
    departureAirport: Airport,
    destinationAirport: Airport,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.depart_label), style = MaterialTheme.typography.labelSmall)
            AirportInfoRow(airport = departureAirport)
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.arrive_label), style = MaterialTheme.typography.labelSmall)
            AirportInfoRow(airport = destinationAirport)
        }
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = if (isFavorite) stringResource(R.string.remove_from_favorites) else stringResource(
                    R.string.add_to_favorites
                ),
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AirportInfoRow(airport: Airport) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = airport.iataCode,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = airport.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
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