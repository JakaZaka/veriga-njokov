package ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import models.ClothingItem
import models.ClothingCategory
import models.Season
import viewmodels.AppViewModel
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddClothingItemDialog(
    onDismissRequest: () -> Unit,
    onItemAdded: (ClothingItem) -> Unit,
    viewModel: AppViewModel
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ClothingCategory.TOPS) }
    var subCategory by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("M") }
    var notes by remember { mutableStateOf("") }
    
    // Track selected seasons with a mutable set
    val selectedSeasons = remember { mutableStateListOf<Season>() }
    
    // Initialize with SUMMER selected by default
    LaunchedEffect(Unit) {
        if (selectedSeasons.isEmpty()) {
            selectedSeasons.add(Season.SUMMER)
        }
    }
    
    // Available sizes
    val availableSizes = listOf("XS", "S", "M", "L", "XL", "XXL")

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()) // Add scrolling
            ) {
                Text("Add New Clothing Item", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Category dropdown with all options
                val categories = ClothingCategory.values()
                var expandedCategory by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { 
                            IconButton(onClick = { expandedCategory = !expandedCategory }) {
                                Icon(
                                    imageVector = if (expandedCategory) 
                                        Icons.Filled.ArrowDropUp 
                                    else 
                                        Icons.Filled.ArrowDropDown,
                                    contentDescription = "Toggle dropdown"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    DropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        categories.forEach { option ->
                            DropdownMenuItem(
                                onClick = {
                                    category = option
                                    expandedCategory = false
                                }
                            ) {
                                Text(text = option.displayName)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // SubCategory field
                OutlinedTextField(
                    value = subCategory,
                    onValueChange = { subCategory = it },
                    label = { Text("Sub-Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Color field
                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Size dropdown with standard sizes
                var sizeExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = size,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Size") },
                        trailingIcon = { 
                            IconButton(onClick = { sizeExpanded = !sizeExpanded }) {
                                Icon(
                                    imageVector = if (sizeExpanded) 
                                        Icons.Filled.ArrowDropUp 
                                    else 
                                        Icons.Filled.ArrowDropDown,
                                    contentDescription = "Toggle dropdown"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    DropdownMenu(
                        expanded = sizeExpanded,
                        onDismissRequest = { sizeExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
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
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Season toggle buttons
                Text(
                    "Seasons",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Create a button for each season
                    Season.values().filter { it != Season.ALL }.forEach { season ->
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
                            Text(season.displayName)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Notes field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismissRequest
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            // Create new ClothingItem with all required parameters
                            val newItem = ClothingItem(
                                name = name,
                                category = category,
                                subCategory = subCategory.ifBlank { null },
                                color = color,
                                size = size,
                                season = if (selectedSeasons.isEmpty()) 
                                           listOf(Season.SUMMER) 
                                         else 
                                           selectedSeasons.toList(),
                                notes = notes,
                                userId = viewModel.currentUserId
                            )
                            onItemAdded(newItem)
                            onDismissRequest()
                        },
                        enabled = name.isNotBlank() && color.isNotBlank() && selectedSeasons.isNotEmpty()
                    ) {
                        Text("Add Item")
                    }
                }
            }
        }
    }
}