package models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ClothingStore(
    @SerialName("_id") 
    val id: String? = null,
    val name: String,
    val website: String? = null,
    val locations: List<Location> = emptyList()
)