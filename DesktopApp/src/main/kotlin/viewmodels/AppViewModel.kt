package viewmodels

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import repositories.DataRepository
import models.*

class AppViewModel {
    private val repository = DataRepository()
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Expose repository data
    val clothingItems = repository.clothingItems
    val clothingStores = repository.clothingStores
    val outfits = repository.outfits
    val weatherData = repository.weatherData
    
    // Expose repository methods
    fun addClothingItem(item: ClothingItem) = repository.addClothingItem(item)
    fun updateClothingItem(item: ClothingItem) = repository.updateClothingItem(item)
    fun deleteClothingItem(id: String) = repository.deleteClothingItem(id)
    fun filterClothingItems(
        category: ClothingCategory? = null,
        season: Season? = null,
        liked: Boolean? = null
    ) = repository.filterClothingItems(category, season, liked)
    
    fun generateDummyData(itemCount: Int = 10, weatherCount: Int = 5) {
        repository.generateDummyClothingItems(itemCount)
        repository.generateDummyWeatherData(weatherCount)
    }
    
    fun clearAllData() = repository.clearAllData()

    // Add this function to expose recommendations to the UI
    fun getWeatherRecommendations(location: String) = repository.getClothingRecommendations(location)
}