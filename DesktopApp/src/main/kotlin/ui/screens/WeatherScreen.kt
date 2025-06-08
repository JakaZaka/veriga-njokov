package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import viewmodels.AppViewModel
import models.Weather  // Manjkal import za Weather razred
import java.text.SimpleDateFormat

@Composable
fun WeatherScreen(viewModel: AppViewModel = remember { AppViewModel() }) {
    var selectedLocation by remember { mutableStateOf("Ljubljana") }
    
    val weatherData by viewModel.weatherData.collectAsState()
    val currentWeather = weatherData
        .filter { it.location.equals(selectedLocation, ignoreCase = true) }
        .maxByOrNull { it.fetchedAt ?: Long.MIN_VALUE } // Provide a default for null fetchedAt
    
    // Dodaj recommendations klic
    val recommendations = viewModel.getWeatherRecommendations(selectedLocation)
    
    // Generiraj weather message tukaj, če ga ne dobiš iz viewModela
    val weatherMessage = if (currentWeather != null) {
        when {
            currentWeather.isRaining -> "It's raining. Don't forget a waterproof jacket!"
            currentWeather.temperature < 5 -> "It's very cold. Wear warm layers!"
            currentWeather.temperature < 15 -> "It's cool outside. Consider a jacket."
            currentWeather.temperature < 25 -> "Pleasant temperature. Light layers recommended."
            else -> "It's warm! Dress lightly."
        }
    } else {
        "No weather data available"
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Weather & Recommendations",
                style = MaterialTheme.typography.h4
            )
            Button(onClick = { 
                // TODO: Scrape new weather data
            }) {
                Text("Refresh Weather")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Location Selector
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Location: ")
            OutlinedButton(onClick = { /* TODO: Show location dropdown */ }) {
                Text(selectedLocation)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            // Current Weather Card
            Card(
                modifier = Modifier.weight(1f),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Current Weather",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (currentWeather != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Location: ${currentWeather.location}")
                        Text("Temperature: ${currentWeather.temperature}°C")
                        
                        if (currentWeather.isRaining) {
                            Text("🌧️ Raining", color = MaterialTheme.colors.primary)
                        }
                        if (currentWeather.isSnowing) {
                            Text("❄️ Snowing", color = MaterialTheme.colors.primary)
                        }
                        
                        currentWeather.fetchedAt?.let { // Ensure fetchedAt is not null
                            Text(
                                text = "Updated: ${java.text.SimpleDateFormat("HH:mm").format(it)}",
                                style = MaterialTheme.typography.caption
                            )
                        }
                    } else {
                        Text("No weather data available")
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Recommendations Card
            Card(
                modifier = Modifier.weight(1f),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Clothing Recommendations",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (recommendations != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Tukaj je napaka - namesto recommendations.weatherMessage uporabi lokalni weatherMessage
                        Text(
                            text = weatherMessage,  // Uporabi lokalni weatherMessage, ki je že definiran
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            // Popravi to vrstico
                            text = "Recommended items: 0", // Hardcoded rešitev za začetek
                            style = MaterialTheme.typography.body2
                        )
                    } else {
                        Text("No recommendations available")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Weather History
        Text(
            text = "Weather History",
            style = MaterialTheme.typography.h6
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(weatherData.sortedByDescending { it.fetchedAt ?: Long.MIN_VALUE }) { weather -> // Provide a default for null fetchedAt
                WeatherHistoryCard(weather)
            }
        }
    }
}

@Composable
private fun WeatherHistoryCard(weather: Weather) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = weather.location,
                    style = MaterialTheme.typography.subtitle1
                )
                weather.fetchedAt?.let { // Ensure fetchedAt is not null
                    Text(
                        text = SimpleDateFormat("dd.MM.yyyy HH:mm").format(it),
                        style = MaterialTheme.typography.caption
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${weather.temperature}°C",
                    style = MaterialTheme.typography.h6
                )
                if (weather.isRaining) Text(" 🌧️")
                if (weather.isSnowing) Text(" ❄️")
            }
        }
    }
}