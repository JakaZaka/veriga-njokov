package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import models.ScrapedClothingItem
import repositories.ClothingItemRepository
import ui.dialogs.EditScrapedItemDialog
import viewmodels.DataGeneratorViewModel
import ui.components.FlowRow

@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
@Composable
fun DataGeneratorScreen() {
    val clothingItemRepository = remember { ClothingItemRepository() }
    val viewModel = remember { DataGeneratorViewModel(clothingItemRepository) }

    // State for the currently edited item
    var itemToEdit by remember { mutableStateOf<ScrapedClothingItem?>(null) }

    // Add a scroll state to make the entire content scrollable
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),  // Add vertical scroll
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Data Generator",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Generator parameters card
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Generate Clothing Items",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Number of items
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Number of items:",
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { if (viewModel.itemCount > 1) viewModel.itemCount-- }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    
                    Text(viewModel.itemCount.toString())
                    
                    IconButton(
                        onClick = { viewModel.itemCount++ }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Color selection
                Text(
                    "Colors:",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp), // Replace mainAxisSpacing
                    verticalArrangement = Arrangement.spacedBy(8.dp)   // Replace crossAxisSpacing
                ) {
                    viewModel.availableColors.forEach { color -> 
                        val isSelected = viewModel.selectedColors.contains(color)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.selectedColors = if (isSelected) {
                                    viewModel.selectedColors - color
                                } else {
                                    viewModel.selectedColors + color
                                }
                            },
                            colors = ChipDefaults.filterChipColors(
                                selectedBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(color)
                        }
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Category selection
                Text(
                    "Categories:",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp), // Replace mainAxisSpacing
                    verticalArrangement = Arrangement.spacedBy(8.dp)   // Replace crossAxisSpacing
                ) {
                    viewModel.availableCategories.forEach { category -> 
                        val isSelected = viewModel.selectedCategories.contains(category)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.selectedCategories = if (isSelected) {
                                    viewModel.selectedCategories - category
                                } else {
                                    viewModel.selectedCategories + category
                                }
                            },
                            colors = ChipDefaults.filterChipColors(
                                selectedBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(category)
                        }
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Size selection
                Text(
                    "Sizes:",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp), // Replace mainAxisSpacing
                    verticalArrangement = Arrangement.spacedBy(8.dp)   // Replace crossAxisSpacing
                ) {
                    viewModel.availableSizes.forEach { size -> 
                        val isSelected = viewModel.selectedSizes.contains(size)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.selectedSizes = if (isSelected) {
                                    viewModel.selectedSizes - size
                                } else {
                                    viewModel.selectedSizes + size
                                }
                            },
                            colors = ChipDefaults.filterChipColors(
                                selectedBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(size)
                        }
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Season selection
                Text(
                    "Seasons:",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp), // Replace mainAxisSpacing
                    verticalArrangement = Arrangement.spacedBy(8.dp)   // Replace crossAxisSpacing
                ) {
                    listOf("spring", "summer", "fall", "winter").forEach { season -> 
                        val isSelected = viewModel.usedSeasons.contains(season)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.usedSeasons = if (isSelected) {
                                    viewModel.usedSeasons - season
                                } else {
                                    viewModel.usedSeasons + season
                                }
                            },
                            colors = ChipDefaults.filterChipColors(
                                selectedBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(season.capitalize())
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Generate button
                Button(
                    onClick = { viewModel.generateItems() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isLoading && viewModel.selectedCategories.isNotEmpty() && 
                              viewModel.selectedColors.isNotEmpty() && viewModel.selectedSizes.isNotEmpty() &&
                              viewModel.usedSeasons.isNotEmpty()
                ) {
                    Text("Generate ${viewModel.itemCount} Items")
                }
            }
        }
        
        // Error message and loading indicator
        viewModel.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        if (viewModel.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Generated items grid
        if (viewModel.generatedItems.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Generated Items (${viewModel.generatedItems.count { it.selected }} selected)",
                    style = MaterialTheme.typography.h6
                )
                
                Button(
                    onClick = { viewModel.saveSelectedItems() },
                    enabled = viewModel.generatedItems.any { it.selected } && !viewModel.isLoading
                ) {
                    Text("Save Selected")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 250.dp),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().height(800.dp)  // Fixed height
            ) {
                items(viewModel.generatedItems) { item ->
                    GeneratedItemCard(
                        item = item,
                        onToggleSelection = { viewModel.toggleItemSelection(item) },
                        onEdit = { itemToEdit = item }
                    )
                }
            }
        }
    }
    
    // Show edit dialog when needed (outside the scroll container)
    itemToEdit?.let { item ->
        EditScrapedItemDialog(
            item = item,
            onDismiss = { itemToEdit = null },
            onSave = { editedItem ->
                // Use the same edit logic as in ScraperViewModel
                viewModel.generatedItems = viewModel.generatedItems.map {
                    if (it == item) editedItem else it
                }
                itemToEdit = null
            }
        )
    }
}

@Composable
fun GeneratedItemCard(
    item: ScrapedClothingItem,
    onToggleSelection: () -> Unit,
    onEdit: () -> Unit
) {
    // Reuse the same card layout as ScrapedItemCard
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelection() }
            .border(
                width = if (item.selected) 2.dp else 0.dp,
                color = if (item.selected) MaterialTheme.colors.primary else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            ),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Title
            Text(
                text = item.name,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Category & Color
            Text(
                text = "Category: ${item.category}",
                style = MaterialTheme.typography.body2
            )
            
            Text(
                text = "Color: ${item.color}",
                style = MaterialTheme.typography.body2
            )
            
            Text(
                text = "Size: ${item.size}",
                style = MaterialTheme.typography.body2
            )
            
            // Seasons
            Text(
                text = "Seasons: ${item.seasons.joinToString { it.capitalize() }}",
                style = MaterialTheme.typography.body2
            )
            
            // Image placeholder with generated color background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(vertical = 8.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(getColorForName(item.color)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Generated ${item.category} item",
                    color = Color.White
                )
            }
            
            // Selection and edit buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = item.selected,
                        onCheckedChange = { onToggleSelection() }
                    )
                    Text("Select")
                }
                
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Item"
                    )
                }
            }
        }
    }
}

// Helper function to generate background colors for preview
fun getColorForName(colorName: String): Color {
    return when (colorName.lowercase()) {
        "black" -> Color.DarkGray
        "white" -> Color.LightGray
        "red" -> Color.Red.copy(alpha = 0.7f)
        "blue" -> Color.Blue.copy(alpha = 0.7f)
        "green" -> Color.Green.copy(alpha = 0.7f)
        "yellow" -> Color(0xFFFFC107)
        "purple" -> Color(0xFF9C27B0).copy(alpha = 0.7f)
        "pink" -> Color(0xFFE91E63).copy(alpha = 0.7f)
        "orange" -> Color(0xFFFF9800).copy(alpha = 0.7f)
        "brown" -> Color(0xFF795548).copy(alpha = 0.7f)
        "gray" -> Color.Gray
        "silver" -> Color(0xFFBDBDBD)
        "gold" -> Color(0xFFFFD700).copy(alpha = 0.7f)
        else -> Color.DarkGray
    }
}

// Extension function to capitalize the first letter
fun String.capitalize(): String {
    return this.replaceFirstChar { it.uppercase() }
}