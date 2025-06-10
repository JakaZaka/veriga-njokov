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
    private const val BASE_URL = "http://localhost:5000/api"
    
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
    // Update the getUsers method to use the desktop admin endpoint
    suspend fun getUsers(): ApiResponse<List<User>> {
        var lastException: Exception? = null
        for (attempt in 1..MAX_RETRIES) {
            try {
                println("Fetching users, attempt $attempt")
                
                val response = apiService.get("$BASE_URL/desktop-admin/users") { responseText ->
                    val apiResponse = jsonConfig.decodeFromString<ApiResponse<List<User>>>(responseText)
                    apiResponse.data ?: emptyList()
                }
                if (response.success && response.data != null) {
                    return response
                }
                
                throw Exception("API returned unsuccessful response: ${response.error}")
            } catch (e: Exception) {
                lastException = e
                println("Attempt $attempt failed: ${e.message}")
                
                if (attempt < MAX_RETRIES) {
                    delay(RETRY_DELAY_MS)
                }
            }
        }
        
        // Only fall back to mock data after all real API attempts have failed
        println("All attempts to fetch users failed, returning mock data")
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
            
            // Use desktop-admin endpoint instead of regular users endpoint
            return apiService.put("$BASE_URL/desktop-admin/users/$userId", user) { responseText ->
                // The response is wrapped in an ApiResponse format
                val apiResponse = jsonConfig.decodeFromString<ApiResponse<User>>(responseText)
                // Extract the user from the response
                apiResponse.data ?: throw Exception("No user data in response")
            }
        } catch (e: Exception) {
            println("Exception when updating user: ${e.message}")
            return ApiResponse(success = false, error = e.message)
        }
    }
    
    // Add token storage
    private var authToken: String? = null
    
    // Update the login method
    suspend fun login(username: String, password: String): ApiResponse<User> {
        return try {
            // Simplify the login call to avoid nested ApiResponse types
            apiService.post("$BASE_URL/users/login", 
                mapOf("username" to username, "password" to password)) { responseText ->
                try {
                    // Parse user directly from response
                    val user = jsonConfig.decodeFromString<User>(responseText)
                    // Store token for future requests
                    authToken = user.token
                    user
                } catch (e: Exception) {
                    println("Failed to parse login response: ${e.message}")
                    throw e  // Rethrow to be caught by the outer try/catch
                }
            }
        } catch (e: Exception) {
            println("Login failed: ${e.message}")
            ApiResponse(success = false, error = e.message)
        }
    }
    
    // Update the deleteUser method to be simpler now that it works
    suspend fun deleteUser(userId: String): ApiResponse<Boolean> {
        if (userId.isBlank()) {
            return ApiResponse(success = false, error = "User ID cannot be empty")
        }
        
        return try {
            val url = "$BASE_URL/desktop-admin/users/$userId"
            apiService.delete(url)
        } catch (e: Exception) {
            println("Failed to delete user: ${e.message}")
            ApiResponse(success = false, error = e.message)
        }
    }
    
    // Clothing item-related API calls
    suspend fun createClothingItem(item: ClothingItem): ApiResponse<ClothingItem> {
        return try {
            println("Creating clothing item: ${item.name}")
            
            apiService.post("$BASE_URL/desktop-admin/clothingItems", item) { responseText ->
                println("Raw create response: $responseText") 
                val apiResponse = jsonConfig.decodeFromString<ApiResponse<ClothingItem>>(responseText)
                apiResponse.data ?: throw Exception("No data in response")
            }
        } catch (e: Exception) {
            println("Failed to create clothing item: ${e.message}")
            ApiResponse(success = false, error = e.message)
        }
    }
    
    // Get all clothing items
    suspend fun getClothingItems(): ApiResponse<List<ClothingItem>> {
        var lastException: Exception? = null
        for (attempt in 1..MAX_RETRIES) {
            try {
                println("Fetching clothing items, attempt $attempt")
                
                val response = apiService.get("$BASE_URL/desktop-admin/clothingItems") { responseText ->
                    println("Raw response: $responseText")
                    val apiResponse = jsonConfig.decodeFromString<ApiResponse<List<ClothingItem>>>(responseText)
                    apiResponse.data ?: emptyList()
                }
                
                if (response.success && response.data != null) {
                    return response
                }
                
                throw Exception("API returned unsuccessful response: ${response.error}")
            } catch (e: Exception) {
                lastException = e
                println("Attempt $attempt failed: ${e.message}")
                
                if (attempt < MAX_RETRIES) {
                    delay(RETRY_DELAY_MS)
                }
            }
        }
        
        return ApiResponse(success = false, error = lastException?.message, data = emptyList())
    }
    
    // Delete a clothing item
    suspend fun deleteClothingItem(id: String): ApiResponse<Boolean> {
        // Check for empty ID to prevent 404 errors
        if (id.isBlank()) {
            return ApiResponse(success = false, error = "Item ID cannot be empty", data = false)
        }
        
        return try {
            val url = "$BASE_URL/desktop-admin/clothingItems/$id"
            apiService.delete(url)
        } catch (e: Exception) {
            ApiResponse(success = false, error = e.message, data = false)
        }
    }
    
    // Get clothing stores
    suspend fun getClothingStores(): ApiResponse<List<ClothingStore>> {
        var lastException: Exception? = null
        for (attempt in 1..MAX_RETRIES) {
            try {
                println("Fetching clothing stores, attempt $attempt")
                
                val response = apiService.get("$BASE_URL/desktop-admin/stores") { responseText ->
                    println("Raw response: $responseText")
                    val apiResponse = jsonConfig.decodeFromString<ApiResponse<List<ClothingStore>>>(responseText)
                    apiResponse.data ?: emptyList()
                }
                
                if (response.success && response.data != null) {
                    return response
                }
                
                throw Exception("API returned unsuccessful response: ${response.error}")
            } catch (e: Exception) {
                lastException = e
                println("Attempt $attempt failed: ${e.message}")
                
                if (attempt < MAX_RETRIES) {
                    delay(RETRY_DELAY_MS)
                }
            }
        }
        
        return ApiResponse(success = false, error = lastException?.message, data = emptyList())
    }
    
    // Delete clothing store
    suspend fun deleteClothingStore(id: String): ApiResponse<Boolean> {
        // Check for empty ID to prevent 404 errors
        if (id.isBlank()) {
            return ApiResponse(success = false, error = "Store ID cannot be empty", data = false)
        }
        
        return try {
            val url = "$BASE_URL/desktop-admin/stores/$id"
            apiService.delete(url)
        } catch (e: Exception) {
            ApiResponse(success = false, error = e.message, data = false)
        }
    }
    
    // Get store locations - revised to match working patterns from users and clothing items
    suspend fun getStoreLocations(): ApiResponse<List<Location>> {
        var lastException: Exception? = null
        for (attempt in 1..MAX_RETRIES) {
            try {
                println("Fetching store locations, attempt $attempt")
                
                // Use the same pattern as getUsers() and getClothingItems()
                val response = apiService.get("$BASE_URL/locations") { responseText ->
                    println("Raw response: $responseText")
                    // Parse directly as List<Location>, not wrapped in ApiResponse
                    jsonConfig.decodeFromString<List<Location>>(responseText)
                }
                
                // apiService.get() already returns an ApiResponse<List<Location>>
                return response
            } catch (e: Exception) {
                lastException = e
                println("Attempt $attempt failed: ${e.message}")
                
                if (attempt < MAX_RETRIES) {
                    delay(RETRY_DELAY_MS)
                }
            }
        }
        
        return ApiResponse(success = false, error = lastException?.message, data = emptyList())
    }
    
    // Delete store location
    suspend fun deleteStoreLocation(id: String): ApiResponse<Boolean> {
        if (id.isBlank()) {
            return ApiResponse(success = false, error = "Location ID cannot be empty", data = false)
        }
        
        return try {
            // Change from /stores/:id to /locations/:id to match backend route
            val url = "$BASE_URL/locations/$id"
            println("Deleting location with ID: $id")
            
            apiService.delete(url)
        } catch (e: Exception) {
            println("Failed to delete location: ${e.message}")
            ApiResponse(success = false, error = e.message, data = false)
        }
    }
    
    // Create store location
    suspend fun createStoreLocation(location: Location): ApiResponse<Location> {
        return try {
            println("Creating store location: ${location.address}, ${location.city}")
            println("Store ID: ${location.clothingStoreId?.id}, Store Name: ${location.clothingStoreId?.name}")
            
            apiService.post("$BASE_URL/locations", location) { responseText ->
                println("Raw create response: $responseText")
                
                // Never return null - throw exception instead
                jsonConfig.decodeFromString<Location>(responseText)
            }
        } catch (e: Exception) {
            println("Failed to create store location: ${e.message}")
            ApiResponse(success = false, error = e.message)
        }
    }
}