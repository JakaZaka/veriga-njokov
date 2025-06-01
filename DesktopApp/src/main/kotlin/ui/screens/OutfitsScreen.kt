package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OutfitsScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Outfits Screen",
            style = MaterialTheme.typography.h4
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Coming Soon - Outfit Management")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = { /* TODO */ }) {
            Text("Create New Outfit")
        }
    }
}