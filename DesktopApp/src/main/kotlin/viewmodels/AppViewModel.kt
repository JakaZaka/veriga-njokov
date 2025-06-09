package viewmodels

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import repositories.DataRepository
import repositories.ClothingItemRepository
import models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel {
    private val repository = DataRepository()
    private val clothingItemRepository = ClothingItemRepository()
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Add current user id property
    var currentUserId: String? = null
    
    // Expose repository data
    val clothingItems = clothingItemRepository.clothingItems
    val clothingItemsLoading = clothingItemRepository.isLoading
    val clothingItemsError = clothingItemRepository.errorMessage
    
    // Filter state
    var selectedCategory by mutableStateOf<ClothingCategory?>(null)
    var selectedSeason by mutableStateOf<Season?>(null)
    var showLikedOnly by mutableStateOf(false)
    var clothingSearchText by mutableStateOf("")
    
    // Filtered items based on current filters
    val filteredClothingItems = mutableStateOf<List<ClothingItem>>(emptyList())
    
    // Add weatherData property
    private val _weatherData = MutableStateFlow<List<Weather>>(emptyList())
    val weatherData = _weatherData.asStateFlow()
    
    // Load clothing items
    fun loadClothingItems() {
        viewModelScope.launch {
            clothingItemRepository.getAllClothingItems()
            applyClothingFilters()
        }
    }
    
    // Add clothing item
    fun addClothingItem(item: ClothingItem) {
        viewModelScope.launch {
            clothingItemRepository.createClothingItem(item)
            applyClothingFilters()
        }
    }
    
    // Apply clothing filters
    fun applyClothingFilters() {
        filteredClothingItems.value = clothingItemRepository.filterItems(
            category = selectedCategory,
            season = selectedSeason,
            searchText = clothingSearchText,
            likedOnly = showLikedOnly
        )
    }
    
    // Clear clothing filters
    fun clearClothingFilters() {
        selectedCategory = null
        selectedSeason = null
        showLikedOnly = false
        clothingSearchText = ""
        applyClothingFilters()
    }
    
    // Also keep the original methods from DataRepository for backwards compatibility
    fun updateClothingItem(item: ClothingItem) = repository.updateClothingItem(item)
    
    // Delete clothing item
    fun deleteClothingItem(id: String) {
        if (id.isBlank()) {
            println("Cannot delete item with empty ID")
            return
        }
        
        viewModelScope.launch {
            println("Deleting clothing item with ID: $id")
            clothingItemRepository.deleteClothingItem(id)
            // Refresh the list after deletion
            loadClothingItems()
        }
    }
    
    fun filterClothingItems(
        category: String? = null,
        season: String? = null,
        favorite: Boolean? = null
    ) = repository.filterClothingItems(category, season, favorite)
    
    // Dummy data generation (might be useful for testing)
    fun generateDummyData(itemCount: Int = 10, weatherCount: Int = 5) {
        repository.generateDummyClothingItems(itemCount)
        repository.generateDummyWeatherData(weatherCount)
    }
    
    fun clearAllData() = repository.clearAllData()
    
    // Add getWeatherRecommendations method
    fun getWeatherRecommendations(location: String): WeatherRecommendation? {
        // Find the most recent weather data for the location
        val currentWeather = _weatherData.value
            .filter { it.location.equals(location, ignoreCase = true) }
            .maxByOrNull { it.fetchedAt ?: 0L }
            
        if (currentWeather != null) {
            // Simple recommendation logic
            val recommendedSeasonString = when {
                currentWeather.temperature < 5.0 -> Season.WINTER.name
                currentWeather.temperature < 15.0 -> Season.FALL.name
                currentWeather.temperature < 25.0 -> Season.SPRING.name
                else -> Season.SUMMER.name
            }
            
            // Find clothing items suitable for this season
            val recommendedItems = clothingItems.value.filter { clothing ->
                clothing.season.any { season ->
                    season.name.equals(recommendedSeasonString, ignoreCase = true)
                }
            }
            
            // Create weather message
            val weatherMessage = when {
                currentWeather.isRaining -> "It's raining. Don't forget a waterproof jacket!"
                currentWeather.temperature < 5.0 -> "It's very cold. Wear warm layers!"
                currentWeather.temperature < 15.0 -> "It's cool outside. Consider a jacket."
                currentWeather.temperature < 25.0 -> "Pleasant temperature. Light layers recommended."
                else -> "It's warm! Dress lightly."
            }
            
            return WeatherRecommendation(
                weatherMessage = weatherMessage,
                recommendedItems = recommendedItems
            )
        }
        
        return null
    }
}