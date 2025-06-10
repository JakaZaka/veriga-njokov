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
import models.ClothingStoreRef
import viewmodels.AppViewModel
import androidx.compose.foundation.clickable

@Composable
fun StoreLocationsScreen(viewModel: AppViewModel = remember { AppViewModel() }) {
    val locations by viewModel.storeLocations.collectAsState()
    val isLoading by viewModel.storeLocationsLoading.collectAsState()
    val errorMessage by viewModel.storeLocationsError.collectAsState()
    
    // State to control the add location dialog
    var showAddDialog by remember { mutableStateOf(false) }
    
    // Load locations when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadStoreLocations()
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header with title and add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Store Locations",
                style = MaterialTheme.typography.h4
            )
            Button(onClick = { showAddDialog = true }) {
                Text("Add Location")
            }
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
    
    // Show custom dialog when needed
    if (showAddDialog) {
        AddStoreLocationDialog(
            onDismissRequest = { showAddDialog = false },
            onLocationAdded = { location ->
                viewModel.addStoreLocation(location)
            },
            viewModel = viewModel  // Add this line to pass the viewModel
        )
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

@Composable
fun AddStoreLocationDialog(
    onDismissRequest: () -> Unit,
    onLocationAdded: (Location) -> Unit,
    viewModel: AppViewModel
) {
    // Define the two specific stores with their IDs
    val zaraStore = ClothingStoreRef(
        id = "6830fc0250fe3e4f4364aef7",
        name = "ZARA", 
        website = "https://www.zara.com/si/"
    )
    
    val hmStore = ClothingStoreRef(
        id = "683c82e19ebb2e3b6cd224b3",
        name = "H&M",
        website = "https://www2.hm.com/en_eur/index.html"
    )
    
    // State variables for the form fields
    var selectedStore by remember { mutableStateOf<ClothingStoreRef?>(null) }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("Slovenia") } // Default value
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Add Store Location") },
        text = {
            Column {
                // Store selection with radio buttons
                Text(
                    "Select Store:",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Radio button for Zara
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedStore == zaraStore,
                        onClick = { selectedStore = zaraStore }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "ZARA",
                        modifier = Modifier.clickable { selectedStore = zaraStore }
                    )
                }
                
                // Radio button for H&M
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedStore == hmStore,
                        onClick = { selectedStore = hmStore }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "H&M",
                        modifier = Modifier.clickable { selectedStore = hmStore }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Address field
                TextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // City field
                TextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Country field
                TextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Error message
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validate and save the new location
                    if (selectedStore == null || address.isBlank() || city.isBlank() || country.isBlank()) {
                        errorMessage = "Please select a store and fill in all fields"
                    } else {
                        errorMessage = null
                        onLocationAdded(
                            Location(
                                id = null, // ID will be generated
                                clothingStoreId = selectedStore!!, // Use the selected store with its correct ID
                                address = address,
                                city = city,
                                country = country
                            )
                        )
                        onDismissRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    )
}