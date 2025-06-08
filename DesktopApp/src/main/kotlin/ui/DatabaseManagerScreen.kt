package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import repositories.UserRepository
import kotlinx.coroutines.launch
import models.*
import ui.dialogs.AddUserDialog
import api.ApiClient  // Add this import

@Composable
fun DatabaseManagerScreen() {
    val userRepository = remember { UserRepository() }
    val scope = rememberCoroutineScope()
    val users by userRepository.users.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    
    // Load users when screen initializes
    LaunchedEffect(Unit) {
        try {
            // First try to login silently with admin credentials
            isLoading = true
            errorMessage = null
            
            // Auto-login with admin credentials
            val loginResult = ApiClient.login("admin", "admin123")
            if (loginResult.success) {
                println("Admin auto-login successful")
            } else {
                println("Admin auto-login failed: ${loginResult.error}")
            }
            
            // Now load the data
            userRepository.getAllUsers()
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    // UI implementation with improved layout
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        // Header with title and add user button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "User Management", 
                style = MaterialTheme.typography.h4
            )
            
            Button(
                onClick = { showAddUserDialog = true }
            ) {
                Text("Add User")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status indicators
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "Unknown error",
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            users.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No users found. Click 'Add User' to create one.",
                        style = MaterialTheme.typography.h6
                    )
                }
            }
            else -> {
                // User list in a scrollable lazy column
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(users) { user ->
                        UserCard(
                            user = user,
                            onDelete = { 
                                // Make sure user.id is not null or empty
                                user.id?.takeIf { it.isNotEmpty() }?.let { id ->
                                    scope.launch {
                                        userRepository.deleteUser(id)
                                    }
                                }
                            },
                            onUpdate = { updatedUser ->
                                scope.launch {
                                    userRepository.updateUser(updatedUser)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            userRepository = userRepository
        )
    }
}

@Composable
fun UserCard(
    user: User,
    onDelete: () -> Unit,
    onUpdate: (User) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User info section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.h6
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Text(
                    text = user.role.uppercase(),
                    style = MaterialTheme.typography.caption,
                    color = if (user.role == "admin") MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Contact info section
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Contact Information",
                        style = MaterialTheme.typography.subtitle1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    user.contactInfo?.let { contactInfo ->
                        Text("Phone: ${contactInfo.phoneNumber ?: "N/A"}")
                        Text("Email: ${contactInfo.emailAddress ?: "N/A"}")
                    } ?: Text("No contact information")
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.subtitle1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    user.location?.let { location ->
                        Text("Address: ${location.address ?: "N/A"}")
                        Text("City: ${location.city ?: "N/A"}")
                        Text("Country: ${location.country}")
                    } ?: Text("No location information")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { 
                        onUpdate(user.copy(
                            username = "${user.username}_updated"
                        )) 
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Update")
                }
                
                Button(
                    onClick = { 
                        println("Delete button clicked for user: ${user.id}")
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
                ) {
                    Text("Delete", color = MaterialTheme.colors.onError)
                }
            }
        }
    }
}
