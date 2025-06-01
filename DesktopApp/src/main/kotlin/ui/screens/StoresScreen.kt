package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StoresScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Clothing Stores",
                style = MaterialTheme.typography.h4
            )
            Button(onClick = { /* TODO: Add store */ }) {
                Text("Add Store")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // TODO: Stores list
        Text("Stores list will be here...")
    }
}