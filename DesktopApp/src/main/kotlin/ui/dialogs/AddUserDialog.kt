package ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import models.ContactInfo
import models.User
import models.UserLocation
import repositories.UserRepository

@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    userRepository: UserRepository
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("Slovenia") }
    
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Add New User", style = MaterialTheme.typography.h6)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Error display
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
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
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password*") },
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
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
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
                            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                                error = "Username, email and password are required"
                                return@Button
                            }
                            
                            isSubmitting = true
                            scope.launch {
                                try {
                                    val newUser = User(
                                        username = username,
                                        email = email,
                                        password = password,
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
                                    
                                    val result = userRepository.createUser(newUser)
                                    if (result != null) {
                                        // Success case - repository returned a User object
                                        onDismiss()
                                        // Note: No need to call getAllUsers() here since the repository already does this
                                    } else {
                                        // Error case - repository returned null
                                        error = "Failed to create user"
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
                            Text("Add User")
                        }
                    }
                }
            }
        }
    }
}