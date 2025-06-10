package models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Outfit(
    @SerialName("_id")
    val id: String? = null,
    val name: String,
    val items: List<OutfitItemRef> = emptyList(),
    val season: List<String> = listOf("all"), // Changed to List<String>
    val occasion: String? = null,
    val liked: Int = 0,
    val likedBy: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val description: String? = null,
    val imageUrl: String? = null,
    val user: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)