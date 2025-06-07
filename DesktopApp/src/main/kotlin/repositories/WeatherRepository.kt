package repositories

import api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import models.ApiResponse
import models.Weather
import network.ApiService

class WeatherRepository(private val apiService: ApiService) {
    private val _weatherData = MutableStateFlow<List<Weather>>(emptyList())
    val weatherData: StateFlow<List<Weather>> = _weatherData.asStateFlow()

    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }

    suspend fun getAllWeatherData(): List<Weather> = withContext(Dispatchers.IO) {
        try {
            val response: ApiResponse<List<Weather>> = apiService.get("weather") { responseText ->
                json.decodeFromString(responseText)
            }
            if (response.success) {
                return@withContext response.data ?: emptyList()
            } else {
                println("Error: ${response.error}")
                return@withContext emptyList()
            }
        } catch (e: Exception) {
            println("Exception when fetching weather data: ${e.message}")
            return@withContext emptyList()
        }
    }

    suspend fun createWeatherData(weather: Weather): Weather? = withContext(Dispatchers.IO) {
        try {
            val response: ApiResponse<Weather> = apiService.post("weather", weather) { responseText ->
                json.decodeFromString<Weather>(responseText)
            }
            return@withContext if (response.success) response.data else null
        } catch (e: Exception) {
            println("Exception when creating weather data: ${e.message}")
            return@withContext null
        }
    }

    suspend fun updateWeatherData(id: String, weather: Weather): Weather? = withContext(Dispatchers.IO) {
        try {
            val response: ApiResponse<Weather> = apiService.put("weather/$id", weather) { responseText ->
                json.decodeFromString<Weather>(responseText)
            }
            return@withContext if (response.success) response.data else null
        } catch (e: Exception) {
            println("Exception when updating weather data $id: ${e.message}")
            return@withContext null
        }
    }

    suspend fun deleteWeatherData(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response: ApiResponse<Boolean> = apiService.delete("weather/$id")
            return@withContext response.success
        } catch (e: Exception) {
            println("Exception when deleting weather data $id: ${e.message}")
            return@withContext false
        }
    }
    
    suspend fun fetchWeatherData(location: String): ApiResponse<Weather> {
        return try {
            apiService.get("https://example.com/weather?location=$location") { responseText ->
                json.decodeFromString<Weather>(responseText)
            }
        } catch (e: Exception) {
            ApiResponse(success = false, error = e.message)
        }
    }
    
    suspend fun updateWeatherData(weather: Weather): ApiResponse<Weather> {
        return try {
            apiService.post("https://example.com/weather/update", weather) { responseText ->
                json.decodeFromString<Weather>(responseText)
            }
        } catch (e: Exception) {
            ApiResponse(success = false, error = e.message)
        }
    }
    
    suspend fun getRecommendations(location: String): ApiResponse<Any> {
        return try {
            apiService.get("https://example.com/weather/recommendations?location=$location") { responseText ->
                json.decodeFromString<Any>(responseText)
            }
        } catch (e: Exception) {
            ApiResponse(success = false, error = e.message)
        }
    }
}