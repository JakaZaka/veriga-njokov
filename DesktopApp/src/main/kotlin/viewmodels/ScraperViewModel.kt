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
    
    fun saveSelectedItems() {
        isLoading = true
        errorMessage = null
        
        coroutineScope.launch {
            try {
                val selectedItems = scrapedItems.filter { it.selected }
                selectedItems.forEach { item ->
                    val clothingItem = ClothingItem(
                        name = item.name,
                        category = mapCategoryToEnum(item.category),  // Convert string to enum
                        color = item.color,
                        size = "M", // Default size
                        season = listOf(models.Season.ALL),  // Add missing season parameter
                        imageUrl = item.imageUrl,
                        notes = "Scraped from H&M: ${item.link}"
                    )
                    repository.createClothingItem(clothingItem)
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
}