package repositories

import api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import models.ClothingStore

class ClothingStoreRepository {
    private val _clothingStores = MutableStateFlow<List<ClothingStore>>(emptyList())
    val clothingStores: StateFlow<List<ClothingStore>> = _clothingStores.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Get all clothing stores from API
    suspend fun getAllClothingStores(): Boolean {
        _isLoading.value = true
        _errorMessage.value = null
        
        return try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.getClothingStores()
            }
            
            if (response.success && response.data != null) {
                _clothingStores.value = response.data
                true
            } else {
                _errorMessage.value = "Failed to fetch clothing stores: ${response.error}"
                false
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }
    
    // Delete clothing store
    suspend fun deleteClothingStore(id: String): Boolean {
        _isLoading.value = true
        _errorMessage.value = null
        
        try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.deleteClothingStore(id)
            }
            
            if (response.success) {
                // Remove the store from the local list
                _clothingStores.value = _clothingStores.value.filter { it.id != id }
                return true
            }
            
            _errorMessage.value = "Failed to delete clothing store: ${response.error}"
            return false
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.message}"
            return false
        } finally {
            _isLoading.value = false
        }
    }
}