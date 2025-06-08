package models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val username: String,
    val email: String,
    val password: String? = null, // Add this field
    val avatar: String? = "",
    val role: String = "user",
    val contactInfo: ContactInfo? = null,
    val location: UserLocation? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class ContactInfo(
    val phoneNumber: String? = null,
    val emailAddress: String? = null
)

@Serializable
data class UserLocation(
    val address: String? = null,
    val city: String? = null,
    val country: String = "Slovenia",
    val coordinates: GeoPoint? = null
)

@Serializable
data class GeoPoint(
    val type: String = "Point",
    val coordinates: List<Double> = listOf(15.6467, 46.5547)
)