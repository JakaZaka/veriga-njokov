package viewmodels

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import repositories.DataRepository
import repositories.ClothingItemRepository
import repositories.ClothingStoreRepository
import repositories.StoreLocationRepository
import models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel {
    private val repository = DataRepository()
    private val clothingItemRepository = ClothingItemRepository()
    private val clothingStoreRepository = ClothingStoreRepository()
    private val storeLocationRepository = StoreLocationRepository()
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Add current user id property
    var currentUserId: String? = null
    
    // Expose repository data
    val clothingItems = clothingItemRepository.clothingItems
    val clothingItemsLoading = clothingItemRepository.isLoading
    val clothingItemsError = clothingItemRepository.errorMessage
    
    // Clothing stores
    val clothingStores = clothingStoreRepository.clothingStores
    val clothingStoresLoading = clothingStoreRepository.isLoading
    val clothingStoresError = clothingStoreRepository.errorMessage
    
    // Store locations
    val storeLocations = storeLocationRepository.locations
    val storeLocationsLoading = storeLocationRepository.isLoading
    val storeLocationsError = storeLocationRepository.errorMessage
    
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
    
    // Load clothing stores
    fun loadClothingStores() {
        viewModelScope.launch {
            clothingStoreRepository.getAllClothingStores()
        }
    }
    
    // Load store locations
    fun loadStoreLocations() {
        viewModelScope.launch {
            storeLocationRepository.getAllLocations()
        }
    }
    
    // Add clothing item
    fun addClothingItem(item: ClothingItem) {
        viewModelScope.launch {
            clothingItemRepository.createClothingItem(item)
            applyClothingFilters()
        }
    }
    
    // Delete clothing item
    fun deleteClothingItem(id: String) {
        if (id.isBlank()) {
            return
        }
        
        viewModelScope.launch {
            clothingItemRepository.deleteClothingItem(id)
            // Refresh the list after deletion
            loadClothingItems()
        }
    }
    
    // Delete clothing store
    fun deleteClothingStore(id: String) {
        if (id.isBlank()) {
            return
        }
        
        viewModelScope.launch {
            clothingStoreRepository.deleteClothingStore(id)
            // Refresh the list after deletion
            loadClothingStores()
        }
    }
    
    // Delete store location
    fun deleteStoreLocation(id: String) {
        if (id.isBlank()) {
            return
        }
        
        viewModelScope.launch {
            storeLocationRepository.deleteLocation(id)
            // Refresh the list after deletion
            loadStoreLocations()
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