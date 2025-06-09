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
fun ClothingItemsScreen(viewModel: AppViewModel = remember { AppViewModel() }) {
    var showAddDialog by remember { mutableStateOf(false) }
    val clothingItems by viewModel.clothingItems.collectAsState()
    val isLoading by viewModel.clothingItemsLoading.collectAsState()
    val errorMessage by viewModel.clothingItemsError.collectAsState()
    
    // Load clothing items when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadClothingItems()
    }
    
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
        
        // Filter Row
        FilterRow(viewModel)
        
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
                            text = "Error loading clothing items",
                            style = MaterialTheme.typography.h6
                        )
                        Text(
                            text = errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.body2
                        )
                        Button(
                            onClick = { viewModel.loadClothingItems() },
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            clothingItems.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No clothing items found. Add some or check your filters.",
                        style = MaterialTheme.typography.h6
                    )
                }
            }
            else -> {
                // Items List with actual data
                ClothingItemsList(
                    clothingItems = viewModel.filteredClothingItems.value,
                    viewModel = viewModel
                )
            }
        }
    }
    
    // Add Item Dialog
    if (showAddDialog) {
        AddClothingItemDialog(
            onDismissRequest = { showAddDialog = false },
            onItemAdded = { 
                viewModel.addClothingItem(it)
                showAddDialog = false 
            },
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FilterRow(viewModel: AppViewModel) {
    Column {
        // Search field
        OutlinedTextField(
            value = viewModel.clothingSearchText,
            onValueChange = { 
                viewModel.clothingSearchText = it
                viewModel.applyClothingFilters() 
            },
            label = { Text("Search by name") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category Filter
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = viewModel.selectedCategory?.displayName ?: "All Categories",
                    onValueChange = {},
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    // Add "All Categories" option
                    DropdownMenuItem(onClick = {
                        viewModel.selectedCategory = null
                        viewModel.applyClothingFilters()
                        categoryExpanded = false
                    }) {
                        Text("All Categories")
                    }
                    
                    // Add all category options
                    for (category in ClothingCategory.values()) {
                        DropdownMenuItem(onClick = {
                            viewModel.selectedCategory = category
                            viewModel.applyClothingFilters()
                            categoryExpanded = false
                        }) {
                            Text(category.displayName)
                        }
                    }
                }
            }
            
            // Season Filter
            var seasonExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = seasonExpanded,
                onExpandedChange = { seasonExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = viewModel.selectedSeason?.displayName ?: "All Seasons",
                    onValueChange = {},
                    label = { Text("Season") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = seasonExpanded) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = seasonExpanded,
                    onDismissRequest = { seasonExpanded = false }
                ) {
                    // Add "All Seasons" option
                    DropdownMenuItem(onClick = {
                        viewModel.selectedSeason = null
                        viewModel.applyClothingFilters()
                        seasonExpanded = false
                    }) {
                        Text("All Seasons")
                    }
                    
                    // Add all season options
                    for (season in Season.values()) {
                        DropdownMenuItem(onClick = {
                            viewModel.selectedSeason = season
                            viewModel.applyClothingFilters()
                            seasonExpanded = false
                        }) {
                            Text(season.displayName)
                        }
                    }
                }
            }
            
            // Liked filter button removed
        }
    }
}

// Update the ClothingItemsList function to remove debugging logs

@Composable
private fun ClothingItemsList(clothingItems: List<ClothingItem>, viewModel: AppViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(clothingItems) { item ->
            ClothingItemCard(
                item = item,
                onDelete = { 
                    // Check both for null and empty ID
                    if (!item.id.isNullOrBlank()) {
                        viewModel.deleteClothingItem(item.id)
                    }
                }
            )
        }
    }
}

// Update the ClothingItemCard to include a delete button
@Composable
private fun ClothingItemCard(item: ClothingItem, onDelete: () -> Unit) {
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
            if (item.season.isNotEmpty()) {
                Text(
                    text = "Season: ${item.season.joinToString { it.displayName }}",
                    style = MaterialTheme.typography.body2
                )
            }
            if (item.imageUrl != null) {
                Text(
                    text = "Has image: ${item.imageUrl?.isNotEmpty() == true}",
                    style = MaterialTheme.typography.caption
                )
            }
            
            // Add Delete button
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