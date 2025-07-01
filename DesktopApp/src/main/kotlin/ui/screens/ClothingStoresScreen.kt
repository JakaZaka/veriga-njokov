package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import models.ClothingStore
import viewmodels.AppViewModel

@Composable
fun ClothingStoresScreen(viewModel: AppViewModel = remember { AppViewModel() }) {
    val clothingStores by viewModel.clothingStores.collectAsState()
    val isLoading by viewModel.clothingStoresLoading.collectAsState()
    val errorMessage by viewModel.clothingStoresError.collectAsState()
    
    // Load clothing stores when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadClothingStores()
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header with title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Clothing Stores",
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
                            text = "Error loading clothing stores",
                            style = MaterialTheme.typography.h6
                        )
                        Text(
                            text = errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.body2
                        )
                        Button(
                            onClick = { viewModel.loadClothingStores() },
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            clothingStores.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No clothing stores found. Add some!",
                        style = MaterialTheme.typography.h6
                    )
                }
            }
            else -> {
                // Stores list with actual data
                ClothingStoresList(
                    clothingStores = clothingStores,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun ClothingStoresList(clothingStores: List<ClothingStore>, viewModel: AppViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(clothingStores) { store ->
            ClothingStoreCard(
                store = store,
                onDelete = { 
                    if (!store.id.isNullOrBlank()) {
                        viewModel.deleteClothingStore(store.id)
                    }
                }
            )
        }
    }
}

@Composable
private fun ClothingStoreCard(store: ClothingStore, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = store.name,
                style = MaterialTheme.typography.h6
            )
            if (store.website != null) {
                Text(
                    text = "Website: ${store.website}",
                    style = MaterialTheme.typography.body2
                )
            }
            if (store.locations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Locations:",
                    style = MaterialTheme.typography.subtitle1
                )
                store.locations.forEach { location ->
                    Text(
                        text = "${location.address}, ${location.city}, ${location.country}",
                        style = MaterialTheme.typography.body2
                    )
                }
            }
            
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