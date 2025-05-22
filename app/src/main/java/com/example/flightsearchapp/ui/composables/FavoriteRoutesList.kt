package com.example.flightsearchapp.ui.composables

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.flightsearchapp.data.local.model.Airport
import com.example.flightsearchapp.data.local.model.FavoriteFlightRoute


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

