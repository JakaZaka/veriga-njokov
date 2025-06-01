package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.dialogs.AddClothingItemDialog
import models.*
import viewmodels.AppViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClothingItemsScreen() {
    var showAddDialog by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Header with Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Clothing Items",
                style = MaterialTheme.typography.h4
            )
            Button(onClick = { showAddDialog = true }) {
                Text("Add Item")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Filter Row (podobno kot v web verziji)
        FilterRow()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Items List
        ClothingItemsList()
    }
    
    // Add Item Dialog
    if (showAddDialog) {
        AddClothingItemDialog(
            onDismiss = { showAddDialog = false },
            onSave = { 
                // TODO: Save item
                showAddDialog = false 
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FilterRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Category Filter
        var selectedCategory by remember { mutableStateOf<ClothingCategory?>(null) }
        
        OutlinedButton(onClick = { /* TODO: Show category selector */ }) {
            Text(selectedCategory?.displayName ?: "All Categories")
        }
        
        // Season Filter
        var selectedSeason by remember { mutableStateOf<Season?>(null) }
        
        OutlinedButton(onClick = { /* TODO: Show season selector */ }) {
            Text(selectedSeason?.displayName ?: "All Seasons")
        }
        
        // Liked Filter - uporabi Button
        var showLikedOnly by remember { mutableStateOf(false) }
        
        Button(
            onClick = { showLikedOnly = !showLikedOnly },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (showLikedOnly) MaterialTheme.colors.primary else MaterialTheme.colors.surface
            )
        ) {
            Text("❤️ Liked Only")
        }
    }
}

@Composable
private fun ClothingItemsList() {
    // TODO: Connect to DataRepository
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Placeholder items
        items(5) { index ->
            ClothingItemCard(
                item = ClothingItem(
                    id = "$index",
                    name = "Sample Item $index",
                    category = ClothingCategory.TOPS,
                    color = "Blue",
                    size = "M"
                )
            )
        }
    }
}

@Composable
private fun ClothingItemCard(item: ClothingItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.h6
            )
            Text(
                text = "Category: ${item.category.displayName}",
                style = MaterialTheme.typography.body2
            )
            if (item.color != null) {
                Text(
                    text = "Color: ${item.color}",
                    style = MaterialTheme.typography.body2
                )
            }
            if (item.size != null) {
                Text(
                    text = "Size: ${item.size}",
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}