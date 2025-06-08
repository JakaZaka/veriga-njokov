package models

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val id: String? = null,
    val clothingStoreId: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String = "Slovenia",
    val coordinates: GeoPoint? = null
)
