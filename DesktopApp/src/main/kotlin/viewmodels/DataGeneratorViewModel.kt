package viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import models.ClothingCategory
import models.ClothingItem
import models.ScrapedClothingItem
import models.Season
import repositories.ClothingItemRepository
import kotlin.random.Random
import com.github.javafaker.Faker

class DataGeneratorViewModel(private val repository: ClothingItemRepository) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val faker = Faker()
    
    // UI State
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var generatedItems by mutableStateOf<List<ScrapedClothingItem>>(emptyList())
    
    // Generation parameters
    var itemCount by mutableStateOf(10)
    
    // Color options
    var availableColors = listOf("Black", "White", "Red", "Blue", "Green", "Yellow", "Purple", "Pink", "Orange", "Brown", "Gray", "Silver", "Gold")
    var selectedColors by mutableStateOf(listOf("Black", "White", "Blue", "Red"))
    
    // Category options
    var availableCategories = listOf(
        "tops", "bottoms", "dresses", "outerwear", 
        "shoes", "accessories", "swimwear"
    )
    var selectedCategories by mutableStateOf(listOf("tops", "bottoms", "dresses"))
    
    // Size options
    var availableSizes = listOf("XS", "S", "M", "L", "XL", "XXL")
    var selectedSizes by mutableStateOf(listOf("S", "M", "L"))
    
    // Season options
    var usedSeasons by mutableStateOf(listOf("summer", "fall"))
    
    // Brand names for more realistic data
    private val brands = listOf(
        "StyleFusion", "Urban Threads", "Elegance", "Casual Corner", 
        "Trendsetter", "Modern Vibes", "Classic Touch", "Fashion Forward",
        "Eco Wear", "Luxe Life", "Street Chic", "Comfort Zone"
    )
    
    fun generateItems() {
        if (itemCount <= 0) {
            errorMessage = "Please specify a valid item count"
            return
        }
        
        isLoading = true
        errorMessage = null
        
        coroutineScope.launch {
            try {
                val items = (1..itemCount).map { index ->
                    // Select random elements from our options
                    val category = selectedCategories.random()
                    val color = selectedColors.random()
                    val size = selectedSizes.random()
                    val seasons = usedSeasons.shuffled().take(Random.nextInt(1, usedSeasons.size + 1))
                    val brand = brands.random()
                    
                    // Generate a descriptive name
                    val itemTypeName = when {
                        category.contains("top") -> faker.commerce().productName()
                        category.contains("bottom") -> listOf("Jeans", "Pants", "Shorts", "Skirt").random()
                        category.contains("dress") -> "${color} ${listOf("Maxi", "Mini", "Cocktail", "Evening", "Summer").random()} Dress"
                        category.contains("outerwear") -> listOf("Jacket", "Coat", "Cardigan", "Blazer").random()
                        category.contains("shoe") -> listOf("Sneakers", "Boots", "Heels", "Flats", "Sandals").random()
                        category.contains("accessory") -> listOf("Necklace", "Bracelet", "Earrings", "Scarf", "Hat", "Sunglasses").random()
                        else -> faker.commerce().productName()
                    }
                    
                    // Create descriptive name
                    val name = "$brand ${adjectiveFor(color)} $itemTypeName"
                    
                    // Generate a fake image URL
                    val imageUrl = "https://example.com/images/${category}/${color.lowercase()}_item_$index.jpg"
                    
                    ScrapedClothingItem(
                        name = name,
                        category = category,
                        imageUrl = imageUrl,
                        color = color,
                        link = "https://example.com/products/item$index",
                        selected = false,
                        size = size,
                        seasons = seasons,
                        notes = "Generated with Kotlin Faker: ${faker.lorem().sentence(3)}"
                    )
                }
                
                generatedItems = items
            } catch (e: Exception) {
                errorMessage = "Failed to generate items: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    private fun adjectiveFor(color: String): String {
        return listOf("Stylish", "Elegant", "Modern", "Classic", "Trendy", "Chic", "Casual", "Comfortable").random()
    }
    
    fun toggleItemSelection(item: ScrapedClothingItem) {
        generatedItems = generatedItems.map {
            if (it == item) it.copy(selected = !it.selected) else it
        }
    }
    
    fun saveSelectedItems() {
        isLoading = true
        errorMessage = null
        
        coroutineScope.launch {
            try {
                val selectedItems = generatedItems.filter { it.selected }
                selectedItems.forEach { item ->
                    val clothingItem = ClothingItem(
                        name = item.name,
                        category = mapCategoryToEnum(item.category),
                        color = item.color,
                        size = item.size,
                        season = item.seasons.map { seasonString -> mapStringToSeason(seasonString) },
                        imageUrl = item.imageUrl,
                        notes = item.notes
                    )
                    repository.createClothingItem(clothingItem)
                }
                
                // After successful save, clear selection
                generatedItems = generatedItems.map { 
                    if (it.selected) it.copy(selected = false) else it 
                }
            } catch (e: Exception) {
                errorMessage = "Failed to save items: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    // Helper method to convert string category to enum
    private fun mapCategoryToEnum(category: String): ClothingCategory {
        return when {
            category.contains("top", ignoreCase = true) -> ClothingCategory.TOPS
            category.contains("bottom", ignoreCase = true) -> ClothingCategory.BOTTOMS
            category.contains("dress", ignoreCase = true) -> ClothingCategory.DRESSES
            category.contains("outerwear", ignoreCase = true) -> ClothingCategory.OUTERWEAR
            category.contains("shoe", ignoreCase = true) -> ClothingCategory.SHOES
            category.contains("accessor", ignoreCase = true) -> ClothingCategory.ACCESSORIES
            else -> ClothingCategory.OTHER
        }
    }
    
    // Helper method to convert string season to enum
    private fun mapStringToSeason(season: String): Season {
        return when (season.lowercase()) {
            "spring" -> Season.SPRING
            "summer" -> Season.SUMMER
            "fall" -> Season.FALL
            "winter" -> Season.WINTER
            else -> Season.ALL
        }
    }
}