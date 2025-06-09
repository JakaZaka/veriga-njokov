package viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.ClothingItem
import models.ScrapedClothingItem
import repositories.ClothingItemRepository
import scraperUtil.HMScraper

class ScraperViewModel(private val repository: ClothingItemRepository) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    // UI State
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var scrapedItems by mutableStateOf<List<ScrapedClothingItem>>(emptyList())
    
    fun startScraping(limit: Int = 30) {
        isLoading = true
        errorMessage = null
        
        coroutineScope.launch {
            try {
                val hmScraper = HMScraper()
                val items = withContext(Dispatchers.IO) {
                    println("Starting scraping with limit: $limit")
                    hmScraper.getWomenClothes(limit)
                }
                scrapedItems = items
                println("Scraping completed. Retrieved ${items.size} items.")
            } catch (e: Exception) {
                errorMessage = "Failed to scrape data: ${e.message}"
                println("Scraper error: ${e.message}")
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
    
    fun toggleItemSelection(item: ScrapedClothingItem) {
        scrapedItems = scrapedItems.map {
            if (it == item) it.copy(selected = !it.selected) else it
        }
    }
    
    // Update the saveSelectedItems method
    fun saveSelectedItems() {
        isLoading = true
        errorMessage = null
        
        coroutineScope.launch {
            try {
                val selectedItems = scrapedItems.filter { it.selected }
                selectedItems.forEach { item ->
                    val clothingItem = ClothingItem(
                        name = item.name,
                        category = mapCategoryToEnum(item.category),
                        color = item.color,
                        size = item.size,
                        season = item.seasons.map { seasonString -> mapStringToSeason(seasonString) }, // Convert String to Season
                        imageUrl = item.imageUrl,
                        notes = item.notes
                    )
                    repository.createClothingItem(clothingItem)
                }
                
                // After successful save, clear selection
                scrapedItems = scrapedItems.map { 
                    if (it.selected) it.copy(selected = false) else it 
                }
            } catch (e: Exception) {
                errorMessage = "Failed to save items: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    private fun mapCategoryToEnum(category: String): models.ClothingCategory {
        return when {
            category.contains("tops", ignoreCase = true) -> models.ClothingCategory.TOPS
            category.contains("bottoms", ignoreCase = true) -> models.ClothingCategory.BOTTOMS
            category.contains("dress", ignoreCase = true) -> models.ClothingCategory.DRESSES
            category.contains("outerwear", ignoreCase = true) -> models.ClothingCategory.OUTERWEAR
            category.contains("shoes", ignoreCase = true) -> models.ClothingCategory.SHOES
            category.contains("accessories", ignoreCase = true) -> models.ClothingCategory.ACCESSORIES
            else -> models.ClothingCategory.OTHER
        }
    }

    // Add this method:
    fun editItem(original: ScrapedClothingItem, edited: ScrapedClothingItem) {
        scrapedItems = scrapedItems.map {
            if (it == original) edited else it
        }
    }

    // Add this helper method to convert String to Season enum
    private fun mapStringToSeason(season: String): models.Season {
        return when (season.lowercase()) {
            "spring" -> models.Season.SPRING
            "summer" -> models.Season.SUMMER
            "fall" -> models.Season.FALL
            "winter" -> models.Season.WINTER
            else -> models.Season.ALL // Default fallback
        }
    }
}