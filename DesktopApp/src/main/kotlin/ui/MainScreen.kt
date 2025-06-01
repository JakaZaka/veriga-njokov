package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.screens.*

enum class AppTab(val title: String) {
    CLOTHING_ITEMS("Clothing Items"),
    STORES("Stores"),
    OUTFITS("Outfits"),
    WEATHER("Weather"),
    SCRAPER("Data Scraper"),
    GENERATOR("Data Generator")
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(AppTab.CLOTHING_ITEMS) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Navigation Tabs
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            AppTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title) }
                )
            }
        }
        
        // Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                AppTab.CLOTHING_ITEMS -> ClothingItemsScreen()
                AppTab.STORES -> StoresScreen()
                AppTab.OUTFITS -> OutfitsScreen()
                AppTab.WEATHER -> WeatherScreen()
                AppTab.SCRAPER -> ScraperScreen()
                AppTab.GENERATOR -> DataGeneratorScreen()
            }
        }
    }
}