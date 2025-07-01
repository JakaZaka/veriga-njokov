package repositories

import api.ApiClient
import models.ApiResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import models.ClothingItem
import models.ClothingCategory
import models.Season

class ClothingItemRepository {
    private val _clothingItems = MutableStateFlow<List<ClothingItem>>(emptyList())
    val clothingItems: StateFlow<List<ClothingItem>> = _clothingItems.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Get all clothing items from API
    suspend fun getAllClothingItems(): Boolean {
        _isLoading.value = true
        _errorMessage.value = null
        
        return try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.getClothingItems()
            }
            
            if (response.success && response.data != null) {
                _clothingItems.value = response.data
                true
            } else {
                _errorMessage.value = "Failed to fetch clothing items: ${response.error}"
                false
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }
    
    // Filter items locally by criteria
    fun filterItems(
        category: ClothingCategory? = null,
        season: Season? = null,
        searchText: String = "",
        likedOnly: Boolean = false
    ): List<ClothingItem> {
        return _clothingItems.value.filter { item ->
            (category == null || item.category == category) &&
            (season == null || item.season.contains(season)) &&
            (searchText.isEmpty() || item.name.contains(searchText, ignoreCase = true)) &&
            (!likedOnly || item.liked)
        }
    }
    
    // Create a new clothing item
    suspend fun createClothingItem(item: ClothingItem): ClothingItem? {
        _isLoading.value = true
        _errorMessage.value = null
        
        try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.createClothingItem(item)
            }
            
            if (response.success && response.data != null) {
                // Add the new item to the current list instead of refetching everything
                val updatedList = _clothingItems.value.toMutableList()
                updatedList.add(response.data)
                _clothingItems.value = updatedList
                return response.data
            }
            
            _errorMessage.value = "Failed to create clothing item: ${response.error}"
            return null
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.message}"
            return null
        } finally {
            _isLoading.value = false
        }
    }
    
    // Update an existing clothing item
    suspend fun updateClothingItem(item: ClothingItem): Boolean {
        _isLoading.value = true
        _errorMessage.value = null
        
        try {
            // In a real implementation, call your update API here
            // For now, we'll just update the local list
            val index = _clothingItems.value.indexOfFirst { it.id == item.id }
            if (index != -1) {
                val updatedList = _clothingItems.value.toMutableList()
                updatedList[index] = item
                _clothingItems.value = updatedList
                return true
            }
            return false
        } catch (e: Exception) {
            _errorMessage.value = "Error updating item: ${e.message}"
            return false
        } finally {
            _isLoading.value = false
        }
    }
    
    // Delete clothing item
    suspend fun deleteClothingItem(id: String): Boolean {
        _isLoading.value = true
        _errorMessage.value = null
        
        try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.deleteClothingItem(id)
            }
            
            if (response.success) {
                // Remove the item from the local list
                _clothingItems.value = _clothingItems.value.filter { it.id != id }
                return true
            }
            
            _errorMessage.value = "Failed to delete clothing item: ${response.error}"
            return false
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.message}"
            return false
        } finally {
            _isLoading.value = false
        }
    }
}