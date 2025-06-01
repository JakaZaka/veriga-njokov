package models

data class Location(
    val address: String? = null,
    val city: String? = null,
    val country: String = "Slovenia",
    val latitude: Double,
    val longitude: Double
)