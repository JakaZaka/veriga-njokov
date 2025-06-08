package repositories

import api.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import models.User

class UserRepository {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    // Get all users from API
    suspend fun getAllUsers() {
        val response = ApiClient.getUsers()
        if (response.success && response.data != null) {
            _users.value = response.data
        } else {
            println("Failed to fetch users: ${response.error}")
        }
    }
    
    // Create a new user
    suspend fun createUser(user: User): User? {
        val response = ApiClient.createUser(user)
        return if (response.success) {
            // If successful, refresh the users list
            getAllUsers()
            response.data
        } else {
            println("Failed to create user: ${response.error}")
            null
        }
    }
    
    // Update an existing user
    suspend fun updateUser(user: User): User? {
        val response = ApiClient.updateUser(user)
        return if (response.success) {
            // If successful, refresh the users list
            getAllUsers()
            response.data
        } else {
            println("Failed to update user: ${response.error}")
            null
        }
    }
    
    // Delete a user
    suspend fun deleteUser(userId: String): Boolean {
        val response = ApiClient.deleteUser(userId)
        if (response.success) {
            // If successful, refresh the users list
            getAllUsers()
            return true
        }
        println("Failed to delete user: ${response.error}")
        return false
    }
    
    // Get a user by ID
    fun getUserById(userId: String): User? {
        return _users.value.find { it.id == userId }
    }
}
