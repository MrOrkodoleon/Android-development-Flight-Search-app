package com.example.flightsearchapp.ui.composables

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.flightsearchapp.data.local.model.Airport

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
