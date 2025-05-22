package com.example.flightsearchapp.ui.composables

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flightsearchapp.data.local.model.Airport
import kotlinx.coroutines.flow.Flow

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