package ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import models.ContactInfo
import models.User
import models.UserLocation
import repositories.UserRepository

@Composable
fun EditUserDialog(
    user: User,
    onDismiss: () -> Unit,
    userRepository: UserRepository
) {
    var username by remember { mutableStateOf(user.username) }
    var email by remember { mutableStateOf(user.email) }
    var role by remember { mutableStateOf(user.role) }
    var phoneNumber by remember { mutableStateOf(user.contactInfo?.phoneNumber ?: "") }
    var contactEmail by remember { mutableStateOf(user.contactInfo?.emailAddress ?: "") }
    var address by remember { mutableStateOf(user.location?.address ?: "") }
    var city by remember { mutableStateOf(user.location?.city ?: "") }
    var country by remember { mutableStateOf(user.location?.country ?: "Slovenia") }
    
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 550.dp),
            elevation = 8.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header with title
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Text("Edit User", style = MaterialTheme.typography.h6)
                    
                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colors.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
                // Scrollable form fields
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Column {
                        // Required fields
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username*") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email*") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        
                        // Role dropdown
                        OutlinedTextField(
                            value = role,
                            onValueChange = { role = it },
                            label = { Text("Role") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        
                        // Optional fields - Contact info
                        Text(
                            "Contact Information",
                            style = MaterialTheme.typography.subtitle1,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                        
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = contactEmail,
                            onValueChange = { contactEmail = it },
                            label = { Text("Contact Email") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        
                        // Optional fields - Location
                        Text(
                            "Location",
                            style = MaterialTheme.typography.subtitle1,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                        
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text("Country") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                    }
                }
                
                // Fixed button row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            // Validate required fields
                            if (username.isBlank() || email.isBlank()) {
                                error = "Username and email are required"
                                return@Button
                            }
                            
                            isSubmitting = true
                            scope.launch {
                                try {
                                    val updatedUser = user.copy(
                                        username = username,
                                        email = email,
                                        role = role,
                                        contactInfo = ContactInfo(
                                            phoneNumber = phoneNumber.takeIf { it.isNotBlank() },
                                            emailAddress = contactEmail.takeIf { it.isNotBlank() }
                                        ),
                                        location = UserLocation(
                                            address = address.takeIf { it.isNotBlank() },
                                            city = city.takeIf { it.isNotBlank() },
                                            country = country
                                        )
                                    )
                                    
                                    val result = userRepository.updateUser(updatedUser)
                                    if (result != null) {
                                        // Success case
                                        onDismiss()
                                    } else {
                                        // Error case
                                        error = "Failed to update user"
                                        isSubmitting = false
                                    }
                                } catch (e: Exception) {
                                    error = e.message ?: "An unexpected error occurred"
                                    isSubmitting = false
                                }
                            }
                        },
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colors.onPrimary
                            )
                        } else {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }
}