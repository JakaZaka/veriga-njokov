package models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Location(
    @SerialName("_id")
    val id: String? = null,
    val clothingStoreId: ClothingStoreRef,
    val address: String,
    val city: String,
    val country: String,
    val coordinates: Coordinates? = null
)

@Serializable
data class ClothingStoreRef(
    @SerialName("_id")
    val id: String,
    val name: String,
    val website: String? = null
)

@Serializable
data class Coordinates(
    val type: String = "Point",
    val coordinates: List<Double> // [longitude, latitude]
)
