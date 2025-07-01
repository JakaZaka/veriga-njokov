package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import models.Outfit
import viewmodels.AppViewModel
import ui.dialogs.AddOutfitDialog

@Composable
fun OutfitsScreen(viewModel: AppViewModel = remember { AppViewModel() }) {
    val outfits by viewModel.outfits.collectAsState()
    val isLoading by viewModel.outfitsLoading.collectAsState()
    val errorMessage by viewModel.outfitsError.collectAsState()
    
    // State to control the add outfit dialog
    var showAddDialog by remember { mutableStateOf(false) }
    
    // Load outfits when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadOutfits()
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header with title and add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Outfits",
                style = MaterialTheme.typography.h4
            )
            Button(onClick = { showAddDialog = true }) {
                Text("Create Outfit")
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
                            text = "Error loading outfits",
                            style = MaterialTheme.typography.h6
                        )
                        Text(
                            text = errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.body2
                        )
                        Button(
                            onClick = { viewModel.loadOutfits() },
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            outfits.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No outfits found. Create some!",
                        style = MaterialTheme.typography.h6
                    )
                }
            }
            else -> {
                // Outfits list with actual data
                OutfitsList(
                    outfits = outfits,
                    viewModel = viewModel
                )
            }
        }
    }
    
    // Add Outfit Dialog
    if (showAddDialog) {
        AddOutfitDialog(
            onDismissRequest = { showAddDialog = false },
            onOutfitAdded = { outfit ->
                viewModel.addOutfit(outfit)
                showAddDialog = false
            },
            viewModel = viewModel
        )
    }
}

@Composable
private fun OutfitsList(outfits: List<Outfit>, viewModel: AppViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(outfits) { outfit ->
            OutfitCard(
                outfit = outfit,
                onDelete = { 
                    if (!outfit.id.isNullOrBlank()) {
                        viewModel.deleteOutfit(outfit.id)
                    }
                }
            )
        }
    }
}

@Composable
private fun OutfitCard(outfit: Outfit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = outfit.name,
                style = MaterialTheme.typography.h6
            )
            
            if (!outfit.description.isNullOrBlank()) {
                Text(
                    text = outfit.description,
                    style = MaterialTheme.typography.body2
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Items: ${outfit.items.size}",
                style = MaterialTheme.typography.caption
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