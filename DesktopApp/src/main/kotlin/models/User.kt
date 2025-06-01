package models

data class User(
    val id: String = "",
    val username: String,
    val email: String,
    val role: UserRole = UserRole.USER,
    val contactInfo: ContactInfo? = null,
    val location: UserLocation? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class ContactInfo(
    val phoneNumber: String? = null,
    val emailAddress: String? = null
)

data class UserLocation(
    val address: String? = null,
    val city: String? = null,
    val country: String = "Slovenia",
    val coordinates: Location? = null
)

enum class UserRole(val displayName: String) {
    USER("User"),
    ADMIN("Admin")
}