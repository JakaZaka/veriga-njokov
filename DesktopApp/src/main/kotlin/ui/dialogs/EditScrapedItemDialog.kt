package ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import models.ClothingCategory
import models.ScrapedClothingItem
import models.Season

@OptIn(ExperimentalMaterialApi::class) // Add this annotation
@Composable
fun EditScrapedItemDialog(
    item: ScrapedClothingItem,
    onDismiss: () -> Unit,
    onSave: (ScrapedClothingItem) -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var category by remember { mutableStateOf(item.category) }
    var color by remember { mutableStateOf(item.color) }
    var size by remember { mutableStateOf("M") } // Default size
    var notes by remember { mutableStateOf("Scraped from H&M: ${item.link}") }
    
    // Available sizes
    val availableSizes = listOf("XS", "S", "M", "L", "XL", "XXL")
    
    // Available seasons with selection state
    val availableSeasons = listOf("spring", "summer", "fall", "winter")
    val selectedSeasons = remember { mutableStateListOf<String>() }
    
    // Initialize with SUMMER selected by default
    LaunchedEffect(Unit) {
        if (item.seasons.isNotEmpty()) {
            selectedSeasons.addAll(item.seasons)
        } else {
            selectedSeasons.add("summer")
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 550.dp),
            elevation = 8.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Text("Edit Item Before Saving", style = MaterialTheme.typography.h6)
                }
                
                // Scrollable content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Column {
                        // Name field
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        
                        // Category field with dropdown
                        var categoryExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.padding(bottom = 8.dp)) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("Category") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            /* Advanced dropdown could be implemented here */
                        }
                        
                        // Color field
                        OutlinedTextField(
                            value = color,
                            onValueChange = { color = it },
                            label = { Text("Color") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        
                        // Size field with dropdown
                        var sizeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = sizeExpanded,
                            onExpandedChange = { sizeExpanded = !sizeExpanded }
                        ) {
                            OutlinedTextField(
                                value = size,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Size") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = sizeExpanded)
                                },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                            
                            ExposedDropdownMenu(
                                expanded = sizeExpanded,
                                onDismissRequest = { sizeExpanded = false }
                            ) {
                                availableSizes.forEach { sizeOption ->
                                    DropdownMenuItem(
                                        onClick = {
                                            size = sizeOption
                                            sizeExpanded = false
                                        }
                                    ) {
                                        Text(sizeOption)
                                    }
                                }
                            }
                        }
                        
                        // Season selection
                        Text(
                            "Seasons",
                            style = MaterialTheme.typography.subtitle1,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableSeasons.forEach { season ->
                                val isSelected = selectedSeasons.contains(season)
                                OutlinedButton(
                                    onClick = {
                                        if (isSelected) {
                                            selectedSeasons.remove(season)
                                        } else {
                                            selectedSeasons.add(season)
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        backgroundColor = if (isSelected) 
                                            MaterialTheme.colors.primary.copy(alpha = 0.1f) 
                                        else 
                                            MaterialTheme.colors.surface
                                    ),
                                    border = ButtonDefaults.outlinedBorder.copy(
                                        width = if (isSelected) 2.dp else 1.dp
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(season.capitalize())
                                }
                            }
                        }
                        
                        // Notes field
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                    }
                }
                
                // Button row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            // Create updated item with edited values
                            val updatedItem = item.copy(
                                name = name,
                                category = category,
                                color = color,
                                // Add the additional properties we're capturing in the edit dialog
                                size = size,
                                seasons = selectedSeasons.toList(),
                                notes = notes
                            )
                            onSave(updatedItem)
                            onDismiss()
                        }
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

// Extension function to capitalize first letter
private fun String.capitalize(): String {
    return this.replaceFirstChar { it.uppercase() }
}