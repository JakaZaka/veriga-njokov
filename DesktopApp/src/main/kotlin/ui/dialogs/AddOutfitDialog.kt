package ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import models.Outfit
import viewmodels.AppViewModel

@Composable
fun AddOutfitDialog(
    onDismissRequest: () -> Unit,
    onOutfitAdded: (Outfit) -> Unit,
    viewModel: AppViewModel
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedItems by remember { mutableStateOf(listOf<String>()) }
    var occasion by remember { mutableStateOf("") }

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
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Occasion field
                OutlinedTextField(
                    value = occasion,
                    onValueChange = { occasion = it },
                    label = { Text("Occasion") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // To fix the items selection, we'd need to implement a multi-select component
                // For now, this is a placeholder
                
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
                            // Create new Outfit with all required parameters
                            val newOutfit = Outfit(
                                name = name,
                                description = description,
                                items = selectedItems, // Empty list for now
                                owner = viewModel.currentUserId ?: "" // Use logged in user or empty string
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