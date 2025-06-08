package ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import models.ClothingItem
import models.ClothingCategory
import models.Season // Import Season enum
import viewmodels.AppViewModel

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
    var size by remember { mutableStateOf("") }
    var season by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                
                // Category dropdown
                // Convert enum to string for display and convert back when needed
                val categories = ClothingCategory.values()
                var expandedCategory by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = category.displayName, // Use displayName
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { option ->
                            DropdownMenuItem(
                                onClick = {
                                    category = option
                                    expandedCategory = false
                                }
                            ) {
                                Text(text = option.displayName) // Use displayName
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
                
                // Size field
                OutlinedTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("Size") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Season field
                OutlinedTextField(
                    value = season,
                    onValueChange = { season = it },
                    label = { Text("Season") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Notes field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                            val seasonEnumList = Season.values().find { it.displayName.equals(season, ignoreCase = true) || it.name.equals(season, ignoreCase = true) }
                                ?.let { listOf(it) } ?: emptyList()

                            val newItem = ClothingItem(
                                name = name,
                                category = category, // Pass enum object directly
                                subCategory = subCategory.ifBlank { null }, // Pass null if blank, or keep as is if blank string is intended
                                color = color,
                                size = size,
                                season = seasonEnumList, // Convert string to List<Season>
                                notes = notes,
                                userId = viewModel.currentUserId // Use userId, and pass null if currentUserId is null
                            )
                            onItemAdded(newItem)
                            onDismissRequest()
                        },
                        enabled = name.isNotBlank() && subCategory.isNotBlank() && season.isNotBlank()
                    ) {
                        Text("Add Item")
                    }
                }
            }
        }
    }
}

// Helper function to create ExposedDropdownMenuBox if not available
@Composable
fun ExposedDropdownMenuBox(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box {
        content()
    }
}

// Helper function to create ExposedDropdownMenu if not available
@Composable
fun ExposedDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    if (expanded) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            content = content
        )
    }
}