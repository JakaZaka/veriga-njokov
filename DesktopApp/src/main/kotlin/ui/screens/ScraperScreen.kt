package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import models.ScrapedClothingItem
import repositories.ClothingItemRepository
import ui.dialogs.EditScrapedItemDialog
import viewmodels.ScraperViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ScraperScreen() {
    val clothingItemRepository = remember { ClothingItemRepository() }
    val viewModel = remember { ScraperViewModel(clothingItemRepository) }
    
    // State for the currently edited item
    var itemToEdit by remember { mutableStateOf<ScrapedClothingItem?>(null) }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Data Scraper",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Scraping buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.startScraping(30) },
                modifier = Modifier.padding(end = 16.dp),
                enabled = !viewModel.isLoading
            ) {
                Text("Scrape H&M Data")
            }
            
            if (viewModel.scrapedItems.isNotEmpty()) {
                Button(
                    onClick = { viewModel.saveSelectedItems() },
                    enabled = viewModel.scrapedItems.any { it.selected } && !viewModel.isLoading
                ) {
                    Text("Save Selected Items")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error message
        viewModel.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // Loading indicator
        if (viewModel.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Scraped items grid
        if (viewModel.scrapedItems.isNotEmpty()) {
            Text(
                "Scraped Items (${viewModel.scrapedItems.count { it.selected }} selected)",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 250.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(viewModel.scrapedItems) { item ->
                    ScrapedItemCard(
                        item = item,
                        onToggleSelection = { viewModel.toggleItemSelection(item) },
                        onEdit = { itemToEdit = item }
                    )
                }
            }
        }
    }
    
    // Show edit dialog when needed
    itemToEdit?.let { item ->
        EditScrapedItemDialog(
            item = item,
            onDismiss = { itemToEdit = null },
            onSave = { editedItem ->
                viewModel.editItem(item, editedItem)
                itemToEdit = null
            }
        )
    }
}

@Composable
fun ScrapedItemCard(
    item: ScrapedClothingItem,
    onToggleSelection: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelection() }
            .border(
                width = if (item.selected) 2.dp else 0.dp,
                color = if (item.selected) MaterialTheme.colors.primary else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            ),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Title
            Text(
                text = item.name,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Category & Color
            Text(
                text = "Category: ${item.category}",
                style = MaterialTheme.typography.body2
            )
            
            Text(
                text = "Color: ${item.color}",
                style = MaterialTheme.typography.body2
            )
            
            // Additional info if edited
            if (item.size != "M" || item.seasons.size > 1 || item.notes != null) {
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                
                if (item.size != "M") {
                    Text(
                        text = "Size: ${item.size}",
                        style = MaterialTheme.typography.body2
                    )
                }
                
                if (item.seasons.size > 1) {
                    Text(
                        text = "Seasons: ${item.seasons.joinToString { it.capitalize() }}",
                        style = MaterialTheme.typography.body2
                    )
                }
            }
            
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(vertical = 8.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Image: ${item.imageUrl.takeLast(20)}...")
            }
            
            // Selection and edit buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = item.selected,
                        onCheckedChange = { onToggleSelection() }
                    )
                    Text("Select")
                }
                
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.secondary
                    )
                ) {
                    Text("Edit")
                }
            }
        }
    }
}