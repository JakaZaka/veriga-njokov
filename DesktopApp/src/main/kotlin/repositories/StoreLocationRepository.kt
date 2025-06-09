package repositories

import api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import models.Location

class StoreLocationRepository {
    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations: StateFlow<List<Location>> = _locations.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Get all store locations from API
    suspend fun getAllLocations(): Boolean {
        _isLoading.value = true
        _errorMessage.value = null
        
        return try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.getStoreLocations()
            }
            
            if (response.success && response.data != null) {
                _locations.value = response.data
                true
            } else {
                _errorMessage.value = "Failed to fetch store locations: ${response.error}"
                false
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }
    
    // Delete location
    suspend fun deleteLocation(id: String): Boolean {
        _isLoading.value = true
        _errorMessage.value = null
        
        try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.deleteStoreLocation(id)
            }
            
            if (response.success) {
                // Remove the location from the local list
                _locations.value = _locations.value.filter { it.id != id }
                return true
            }
            
            _errorMessage.value = "Failed to delete store location: ${response.error}"
            return false
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.message}"
            return false
        } finally {
            _isLoading.value = false
        }
    }
}