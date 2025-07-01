package ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import models.Outfit
import models.OutfitItemRef
import models.Season
import viewmodels.AppViewModel

@Composable
fun AddOutfitDialog(
    onDismissRequest: () -> Unit,
    onOutfitAdded: (Outfit) -> Unit,
    viewModel: AppViewModel
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var occasion by remember { mutableStateOf("casual") } // Default value
    
    // Default item ID - this is a placeholder that would normally come from selection
    val defaultItemId = "682cba6cab337b6c852ecc05" // Use a real item ID from your database
    
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Add New Outfit", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Outfit Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Occasion field
                OutlinedTextField(
                    value = occasion,
                    onValueChange = { occasion = it },
                    label = { Text("Occasion (e.g., casual, formal, sport)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Info text about item selection
                Text(
                    "Note: This simplified version will use a default item.",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
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
                            // Create a simple OutfitItemRef with just the item ID
                            val itemRef = OutfitItemRef(
                                item = defaultItemId  // This is correct since item is now a String
                            )
                            
                            // Create new Outfit with properly structured data
                            val newOutfit = Outfit(
                                name = name,
                                description = description,
                                items = listOf(itemRef), // Include the default item
                                season = listOf("all"), // Use string seasons
                                occasion = occasion,
                                user = viewModel.currentUserId ?: "683cb5ef0457c4ac1ad75b13" // Use a default user ID if current is null
                            )
                            onOutfitAdded(newOutfit)
                            onDismissRequest()
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Text("Add Outfit")
                    }
                }
            }
        }
    }
}