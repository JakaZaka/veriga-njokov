package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import models.*
import repositories.UserRepository

@Composable
fun UsersScreen() {
    val userRepository = remember { UserRepository() }
    val scope = rememberCoroutineScope()
    val users by userRepository.users.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Load users when screen initializes
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            errorMessage = null
            userRepository.getAllUsers()
        } catch (e: Exception) {
            errorMessage = "Failed to load users: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Text(
            text = "User Management",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(16.dp)
        )
        
        if (isLoading) {
            // Show loading indicator
            CircularProgressIndicator(
                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
            )
        } else if (errorMessage != null) {
            // Show error message
            Text(
                text = errorMessage ?: "Unknown error",
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(16.dp)
            )
        } else if (users.isEmpty()) {
            // Show empty state
            Text(
                text = "No users found. Add a new user to get started.",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            // User list
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(users) { user ->
                    UserItem(
                        user = user,
                        onDelete = {
                            scope.launch {
                                userRepository.deleteUser(user.id ?: "")
                            }
                        }
                    )
                }
            }
        }
        
        // Add user button
        Button(
            onClick = {
                scope.launch {
                    val newUser = User(
                        username = "new_user",
                        email = "new@example.com",
                        contactInfo = ContactInfo(
                            phoneNumber = "123456789",
                            emailAddress = "contact@example.com"
                        ),
                        location = UserLocation(
                            address = "123 Main St",
                            city = "Example City",
                            country = "Slovenia"
                        )
                    )
                    userRepository.createUser(newUser)
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Add User")
        }
    }
}

@Composable
fun UserItem(user: User, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Username: ${user.username}")
            Text("Email: ${user.email}")
            
            // Display contact info if available
            user.contactInfo?.let { contactInfo ->
                Text("Phone: ${contactInfo.phoneNumber ?: "N/A"}")
                Text("Contact Email: ${contactInfo.emailAddress ?: "N/A"}")
            }
            
            // Display location if available
            user.location?.let { location ->
                Text("Address: ${location.address ?: "N/A"}")
                Text("City: ${location.city ?: "N/A"}")
                Text("Country: ${location.country}")
            }
            
            // Delete button
            Button(
                onClick = onDelete,
                modifier = Modifier.align(androidx.compose.ui.Alignment.End)
            ) {
                Text("Delete")
            }
        }
    }
}
