package ui

import androidx.compose.foundation.background
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
import ui.dialogs.EditUserDialog  // Add this import
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
            isLoading = true
            errorMessage = null
            
            // Remove the login code and just load data directly
            userRepository.getAllUsers()
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    // UI implementation with improved layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colors.background)
    ) {
        // Header with title and add user button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "User Management",
                style = MaterialTheme.typography.h4,
                color = MaterialTheme.colors.primary
            )
            Button(
                onClick = { showAddUserDialog = true },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Add User", color = MaterialTheme.colors.onPrimary)
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
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    items(users) { user ->
                        UserCard(
                            user = user,
                            onDelete = { 
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
                            },
                            userRepository = userRepository
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
    onUpdate: (User) -> Unit,
    userRepository: UserRepository // Add this parameter
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colors.surface, shape = MaterialTheme.shapes.medium),
        elevation = 6.dp
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
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
            )
            
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
                    onClick = { showEditDialog = true },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.primary),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Edit")
                }
                Button(
                    onClick = { 
                        println("Delete button clicked for user: ${user.id}")
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Delete", color = MaterialTheme.colors.onError)
                }
            }
        }
    }
    
    // Show edit dialog when needed
    if (showEditDialog) {
        EditUserDialog(
            user = user,
            onDismiss = { showEditDialog = false },
            userRepository = userRepository
        )
    }
}