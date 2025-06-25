package models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class OutfitItemRef(
    @SerialName("_id")
    val id: String? = null,
    @SerialName("item")
    val item: String  // Store just the ID as a string
)