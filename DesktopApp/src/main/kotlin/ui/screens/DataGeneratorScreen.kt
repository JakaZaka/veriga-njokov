package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import viewmodels.AppViewModel

@Composable
fun DataGeneratorScreen(viewModel: AppViewModel = remember { AppViewModel() }) {
    var clothingItemCount by remember { mutableStateOf("10") }
    var weatherDataCount by remember { mutableStateOf("5") }
    var storeCount by remember { mutableStateOf("3") }
    var outfitCount by remember { mutableStateOf("5") }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Data Generator",
            style = MaterialTheme.typography.h4
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(0.6f),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Generate Dummy Data",
                    style = MaterialTheme.typography.h6
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Clothing Items
                GeneratorSection(
                    title = "Clothing Items",
                    count = clothingItemCount,
                    onCountChange = { clothingItemCount = it },
                    onGenerate = { 
                        viewModel.generateDummyData(
                            itemCount = clothingItemCount.toIntOrNull() ?: 10,
                            weatherCount = 0
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Weather Data
                GeneratorSection(
                    title = "Weather Data",
                    count = weatherDataCount,
                    onCountChange = { weatherDataCount = it },
                    onGenerate = { 
                        viewModel.generateDummyData(
                            itemCount = 0,
                            weatherCount = weatherDataCount.toIntOrNull() ?: 5
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Stores
                GeneratorSection(
                    title = "Clothing Stores",
                    count = storeCount,
                    onCountChange = { storeCount = it },
                    onGenerate = { 
                        // TODO: Generate stores
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Outfits
                GeneratorSection(
                    title = "Outfits",
                    count = outfitCount,
                    onCountChange = { outfitCount = it },
                    onGenerate = { 
                        // TODO: Generate outfits
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Divider()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Generate All
                Button(
                    onClick = {
                        viewModel.generateDummyData(
                            itemCount = clothingItemCount.toIntOrNull() ?: 10,
                            weatherCount = weatherDataCount.toIntOrNull() ?: 5
                        )
                        // TODO: Generate stores and outfits
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate All Data")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Clear All
                OutlinedButton(
                    onClick = { viewModel.clearAllData() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear All Data")
                }
            }
        }
    }
}

@Composable
private fun GeneratorSection(
    title: String,
    count: String,
    onCountChange: (String) -> Unit,
    onGenerate: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f)
        )
        
        OutlinedTextField(
            value = count,
            onValueChange = onCountChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(80.dp),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Button(onClick = onGenerate) {
            Text("Generate")
        }
    }
}