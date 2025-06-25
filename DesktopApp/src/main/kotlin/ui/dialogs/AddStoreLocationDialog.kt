package ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import models.ClothingStoreRef
import models.Location
import viewmodels.AppViewModel

@Composable
fun AddStoreLocationDialog(
    onDismissRequest: () -> Unit,
    onLocationAdded: (Location) -> Unit,
    viewModel: AppViewModel = remember { AppViewModel() }
) {
    // State variables for the form fields
    var selectedStore by remember { mutableStateOf<ClothingStoreRef?>(null) }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("Slovenia") } // Default value
    
    // Define the two specific stores with their IDs
    val stores = listOf(
        ClothingStoreRef(
            id = "6830fc0250fe3e4f4364aef7", 
            name = "ZARA",
            website = "https://www.zara.com/si/"
        ),
        ClothingStoreRef(
            id = "683c82e19ebb2e3b6cd224b3",
            name = "H&M", 
            website = "https://www2.hm.com/en_eur/index.html"
        )
    )
    
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Add New Store Location", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Store selection
                Text(
                    "Select Store",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Store selection buttons (hardcoded options)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    stores.forEach { store ->
                        val isSelected = selectedStore?.id == store.id
                        OutlinedButton(
                            onClick = { selectedStore = store },
                            colors = ButtonDefaults.outlinedButtonColors(
                                backgroundColor = if (isSelected) 
                                    MaterialTheme.colors.primary.copy(alpha = 0.1f) 
                                else 
                                    MaterialTheme.colors.surface
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(store.name)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Address field
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // City field
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Country field
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Show selected store ID for debugging
                selectedStore?.let {
                    Text(
                        "Selected Store ID: ${it.id}",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Button row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismissRequest
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            // Create new location with the selected store
                            val newLocation = Location(
                                clothingStoreId = selectedStore!!,
                                address = address,
                                city = city,
                                country = country
                            )
                            onLocationAdded(newLocation)
                            onDismissRequest()
                        },
                        enabled = selectedStore != null && address.isNotBlank() && city.isNotBlank() && country.isNotBlank()
                    ) {
                        Text("Add Location")
                    }
                }
            }
        }
    }
}