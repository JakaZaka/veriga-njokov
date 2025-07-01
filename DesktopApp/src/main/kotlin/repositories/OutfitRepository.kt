package repositories

import api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import models.Outfit

class OutfitRepository {
    private val _outfits = MutableStateFlow<List<Outfit>>(emptyList())
    val outfits: StateFlow<List<Outfit>> = _outfits
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    // Get all outfits
    suspend fun getAllOutfits(): Boolean {
        _isLoading.value = true
        _errorMessage.value = null
        
        try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.getOutfits()
            }
            
            if (response.success && response.data != null) {
                _outfits.value = response.data
                return true
            }
            
            _errorMessage.value = "Failed to fetch outfits: ${response.error}"
            return false
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.message}"
            return false
        } finally {
            _isLoading.value = false
        }
    }
    
    // Create a new outfit
    suspend fun createOutfit(outfit: Outfit): Boolean {
        _isLoading.value = true
        _errorMessage.value = null
        
        try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.createOutfit(outfit)
            }
            
            if (response.success && response.data != null) {
                _outfits.value = _outfits.value + response.data
                return true
            }
            
            _errorMessage.value = "Failed to create outfit: ${response.error}"
            return false
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.message}"
            return false
        } finally {
            _isLoading.value = false
        }
    }
    
    // Delete an outfit
    suspend fun deleteOutfit(id: String): Boolean {
        _isLoading.value = true
        _errorMessage.value = null
        
        try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.deleteOutfit(id)
            }
            
            if (response.success) {
                _outfits.value = _outfits.value.filter { it.id != id }
                return true
            }
            
            _errorMessage.value = "Failed to delete outfit: ${response.error}"
            return false
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.message}"
            return false
        } finally {
            _isLoading.value = false
        }
    }
}