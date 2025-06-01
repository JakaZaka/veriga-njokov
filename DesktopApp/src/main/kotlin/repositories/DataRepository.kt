package repositories

import models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.random.Random

class DataRepository {
    // State flows za reactive UI
    private val _clothingItems = MutableStateFlow<List<ClothingItem>>(emptyList())
    val clothingItems: StateFlow<List<ClothingItem>> = _clothingItems.asStateFlow()
    
    private val _clothingStores = MutableStateFlow<List<ClothingStore>>(emptyList())
    val clothingStores: StateFlow<List<ClothingStore>> = _clothingStores.asStateFlow()
    
    private val _outfits = MutableStateFlow<List<Outfit>>(emptyList())
    val outfits: StateFlow<List<Outfit>> = _outfits.asStateFlow()
    
    private val _weatherData = MutableStateFlow<List<Weather>>(emptyList())
    val weatherData: StateFlow<List<Weather>> = _weatherData.asStateFlow()
    
    // CRUD operacije
    fun addClothingItem(item: ClothingItem) {
        val newItem = item.copy(id = UUID.randomUUID().toString())
        _clothingItems.value = _clothingItems.value + newItem
    }
    
    fun updateClothingItem(updatedItem: ClothingItem) {
        _clothingItems.value = _clothingItems.value.map { 
            if (it.id == updatedItem.id) updatedItem else it 
        }
    }
    
    fun deleteClothingItem(itemId: String) {
        _clothingItems.value = _clothingItems.value.filter { it.id != itemId }
    }
    
    fun addOutfit(outfit: Outfit) {
        val newOutfit = outfit.copy(id = UUID.randomUUID().toString())
        _outfits.value = _outfits.value + newOutfit
    }
    
    fun deleteOutfit(outfitId: String) {
        _outfits.value = _outfits.value.filter { it.id != outfitId }
    }
    
    // Dummy data generation
    fun generateDummyClothingItems(count: Int) {
        val dummyItems = (1..count).map { index ->
            ClothingItem(
                id = UUID.randomUUID().toString(),
                name = "Sample Item $index",
                category = ClothingCategory.values().random(),
                color = listOf("Red", "Blue", "Green", "Black", "White").random(),
                size = listOf("XS", "S", "M", "L", "XL").random(),
                season = listOf(Season.values().random()),
                fromShop = Random.nextInt(1, 11) > 7, // 30% chance from shop
                price = if (Random.nextInt(1, 11) > 7) Random.nextDouble(10.0, 200.0) else null
            )
        }
        _clothingItems.value = _clothingItems.value + dummyItems
    }
    
    fun generateDummyWeatherData(count: Int) {
        val locations = listOf("Ljubljana", "Maribor", "Celje", "Kranj")
        val dummyWeather = (1..count).map {
            Weather(
                id = UUID.randomUUID().toString(),
                location = locations.random(),
                temperature = Random.nextDouble(-10.0, 35.0),
                isRaining = Random.nextInt(1, 11) > 7, // 30% chance of rain
                isSnowing = Random.nextInt(1, 11) > 9, // 10% chance of snow
                fetchedAt = System.currentTimeMillis() - Random.nextLong(0, 86400000L) // Random time in last 24h
            )
        }
        _weatherData.value = _weatherData.value + dummyWeather
    }
    
    fun clearAllData() {
        _clothingItems.value = emptyList()
        _clothingStores.value = emptyList()
        _outfits.value = emptyList()
        _weatherData.value = emptyList()
    }
    
    // Prestavi to funkcijo V RAZRED - tu je bila napaka
    fun getClothingRecommendations(location: String): ClothingRecommendations? {
        val weather = _weatherData.value
            .filter { it.location.equals(location, ignoreCase = true) }
            .maxByOrNull { it.fetchedAt } ?: return null
        
        // Priporoči oblačila glede na temperaturo in vreme
        val recommendedSeason = when {
            weather.temperature < 5.0 -> Season.WINTER
            weather.temperature < 15.0 -> Season.FALL
            weather.temperature < 25.0 -> Season.SPRING
            else -> Season.SUMMER
        }
        
        // Filtriraj oblačila po sezoni
        val recommendedItems = _clothingItems.value.filter { clothing ->
            clothing.season.contains(recommendedSeason) || clothing.season.contains(Season.ALL)
        }
        
        // Generiraj sporočilo glede na vreme
        val weatherMessage = when {
            weather.isRaining -> "It's raining. Don't forget a waterproof jacket!"
            weather.isSnowing -> "It's snowing. Wear warm, waterproof clothing!"
            weather.temperature < 5.0 -> "It's very cold. Wear warm layers!"
            weather.temperature < 15.0 -> "It's cool outside. Consider a jacket."
            weather.temperature < 25.0 -> "Pleasant temperature. Light layers recommended."
            else -> "It's warm! Dress lightly."
        }
        
        return ClothingRecommendations(
            weather = weather,
            recommendedItems = recommendedItems,
            weatherMessage = weatherMessage
        )
    }
    
    // Dodaj to metodo v DataRepository razred
    fun filterClothingItems(
        category: ClothingCategory? = null,
        season: Season? = null,
        liked: Boolean? = null
    ): List<ClothingItem> {
        return _clothingItems.value.filter { item ->
            (category == null || item.category == category) &&
            (season == null || item.season.contains(season)) &&
            (liked == null || item.liked == liked)
        }
    }
}

// Ta razred naj ostane izven glavnega razreda
data class ClothingRecommendations(
    val weather: Weather,
    val recommendedItems: List<ClothingItem>,
    val weatherMessage: String
)