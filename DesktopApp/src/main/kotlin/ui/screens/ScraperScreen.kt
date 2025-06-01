package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScraperScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Data Scraper",
            style = MaterialTheme.typography.h4
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Scraping buttons
        Button(
            onClick = { /* TODO: Scrape weather */ },
            modifier = Modifier.fillMaxWidth(0.3f)
        ) {
            Text("Scrape Weather Data")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { /* TODO: Scrape Zara */ },
            modifier = Modifier.fillMaxWidth(0.3f)
        ) {
            Text("Scrape Zara Data")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { /* TODO: Scrape H&M */ },
            modifier = Modifier.fillMaxWidth(0.3f)
        ) {
            Text("Scrape H&M Data")
        }
    }
}