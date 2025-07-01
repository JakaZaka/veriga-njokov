package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ui.screens.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(AppTab.USERS) } // Default to Users tab

    Column(modifier = Modifier.fillMaxSize()) {
        // Navigation Tabs - Centered with BoxWithConstraints
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val maxWidth = this.maxWidth

            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                modifier = Modifier
                    .fillMaxWidth() // Ensure background color spans the entire width
                    .width(maxWidth.coerceAtMost(900.dp)),
                backgroundColor = MaterialTheme.colors.primarySurface,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                        color = MaterialTheme.colors.primary
                    )
                }
            ) {
                AppTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .wrapContentWidth(), // Prevent vertical text layout
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.body1
                                )
                            }
                        }
                    )
                }
            }
        }

        // Content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                AppTab.USERS -> DatabaseManagerScreen()
                AppTab.CLOTHING_ITEMS -> ClothingItemsScreen()
                AppTab.STORES -> StoreLocationsScreen()
                AppTab.OUTFITS -> OutfitsScreen()
                AppTab.SCRAPER -> ScraperScreen()
                AppTab.GENERATOR -> DataGeneratorScreen()
            }
        }
    }
}

// Add icons to AppTab
enum class AppTab(val title: String, val icon: ImageVector) {
    USERS("Users", Icons.Default.Person),
    CLOTHING_ITEMS("Clothing Items", Icons.Default.ShoppingCart),
    STORES("Stores", Icons.Default.Store),
    OUTFITS("Outfits", Icons.Default.Style),
    SCRAPER("Data Scraper", Icons.Default.CloudDownload),
    GENERATOR("Data Generator", Icons.Default.Build)
}