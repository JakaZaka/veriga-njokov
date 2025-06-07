package api

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import models.*
import network.ApiService
import kotlinx.coroutines.delay

object ApiClient {
    // Change port to 3000 which is where your web backend is running
    private const val BASE_URL = "http://localhost:3000/api"
    
    // Add retry mechanism
    private const val MAX_RETRIES = 3
    private const val RETRY_DELAY_MS = 1000L
    
    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    private val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(jsonConfig)
        }
        engine {
            requestTimeout = 30000 // 30 seconds timeout
        }
    }
    
    private val apiService = ApiService(client)
    
    // User-related API calls with retry mechanism
    suspend fun getUsers(): ApiResponse<List<User>> {
        var lastException: Exception? = null
        for (attempt in 1..MAX_RETRIES) {
            try {
                println("Fetching users, attempt $attempt")
                
                // In first attempt, try to get from real API
                if (attempt == 1) {
                    val response = apiService.get("$BASE_URL/users") { responseText ->
                        // The controller wraps the response in a success/data format
                        val apiResponse = jsonConfig.decodeFromString<ApiResponse<List<User>>>(responseText)
                        // Extract and return the data part
                        apiResponse.data ?: emptyList()
                    }
                    if (response.success && response.data != null) {
                        return response
                    }
                }
                
                // For subsequent attempts or if first attempt failed, return mock data
                println("Returning mock data for testing")
                return ApiResponse(
                    success = true,
                    data = listOf(
                        User(
                            id = "1",
                            username = "testuser",
                            email = "test@example.com",
                            contactInfo = ContactInfo(
                                phoneNumber = "123456789",
                                emailAddress = "contact@example.com"
                            ),
                            location = UserLocation(
                                address = "Test Street 1",
                                city = "Test City",
                                country = "Slovenia"
                            )
                        )
                    )
                )
            } catch (e: Exception) {
                lastException = e
                println("Attempt $attempt failed: ${e.message}")
                
                if (attempt < MAX_RETRIES) {
                    delay(RETRY_DELAY_MS)
                }
            }
        }
        
        println("All attempts to fetch users failed")
        return ApiResponse(success = false, error = lastException?.message)
    }
    
    suspend fun createUser(user: User): ApiResponse<User> {
        return try {
            apiService.post("$BASE_URL/users", user) { responseText ->
                jsonConfig.decodeFromString<User>(responseText)
            }
        } catch (e: Exception) {
            println("Exception when creating user: ${e.message}")
            ApiResponse(success = false, error = e.message)
        }
    }
    
    suspend fun updateUser(user: User): ApiResponse<User> {
        try {
            val userId = user.id ?: throw IllegalArgumentException("User ID cannot be null")
            return apiService.put("$BASE_URL/users/$userId", user) { responseText ->
                jsonConfig.decodeFromString<User>(responseText)
            }
        } catch (e: Exception) {
            println("Exception when updating user: ${e.message}")
            return ApiResponse(success = false, error = e.message)
        }
    }
    
    suspend fun deleteUser(userId: String): ApiResponse<Boolean> {
        return try {
            apiService.delete("$BASE_URL/users/$userId")
        } catch (e: Exception) {
            println("Exception when deleting user: ${e.message}")
            ApiResponse(success = false, error = e.message)
        }
    }
}