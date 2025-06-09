package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import models.Location
import viewmodels.AppViewModel

@Composable
fun StoreLocationsScreen(viewModel: AppViewModel = remember { AppViewModel() }) {
    val locations by viewModel.storeLocations.collectAsState()
    val isLoading by viewModel.storeLocationsLoading.collectAsState()
    val errorMessage by viewModel.storeLocationsError.collectAsState()
    
    // Load locations when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadStoreLocations()
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header with title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Store Locations",
                style = MaterialTheme.typography.h4
            )
            // Add button will be implemented later
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status area (loading, error, etc)
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Card(
                    backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Error loading store locations",
                            style = MaterialTheme.typography.h6
                        )
                        Text(
                            text = errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.body2
                        )
                        Button(
                            onClick = { viewModel.loadStoreLocations() },
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            locations.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No store locations found. Add some!",
                        style = MaterialTheme.typography.h6
                    )
                }
            }
            else -> {
                // Locations list with actual data
                LocationsList(
                    locations = locations,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun LocationsList(locations: List<Location>, viewModel: AppViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(locations) { location ->
            LocationCard(
                location = location,
                onDelete = { 
                    if (!location.id.isNullOrBlank()) {
                        viewModel.deleteStoreLocation(location.id)
                    }
                }
            )
        }
    }
}

@Composable
private fun LocationCard(location: Location, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = location.clothingStoreId.name,
                style = MaterialTheme.typography.h6
            )
            if (location.clothingStoreId.website != null) {
                Text(
                    text = "Website: ${location.clothingStoreId.website}",
                    style = MaterialTheme.typography.subtitle2
                )
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = location.address,
                style = MaterialTheme.typography.body1
            )
            Text(
                text = "${location.city}, ${location.country}",
                style = MaterialTheme.typography.body1
            )
            
            // Delete button
            Button(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.End).padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
            ) {
                Text("Delete")
            }
        }
    }
}