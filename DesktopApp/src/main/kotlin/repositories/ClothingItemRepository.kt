package repositories

import api.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import models.ClothingItem

class ClothingItemRepository {
    private val _clothingItems = MutableStateFlow<List<ClothingItem>>(emptyList())
    val clothingItems: StateFlow<List<ClothingItem>> = _clothingItems.asStateFlow()
    
    // Get all clothing items from API
    suspend fun getAllClothingItems() {
        val response = ApiClient.getClothingItems()
        if (response.success && response.data != null) {
            _clothingItems.value = response.data
        } else {
            println("Failed to fetch clothing items: ${response.error}")
        }
    }
    
    // Create a new clothing item
    suspend fun createClothingItem(item: ClothingItem): ClothingItem? {
        val response = ApiClient.createClothingItem(item)
        if (response.success && response.data != null) {
            getAllClothingItems() // Refresh the list
            return response.data
        }
        println("Failed to create clothing item: ${response.error}")
        return null
    }
}