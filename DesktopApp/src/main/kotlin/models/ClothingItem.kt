package models

import kotlinx.serialization.Serializable

@Serializable
data class ClothingItem(
    val id: String? = null,
    val name: String,
    val category: ClothingCategory,
    val subCategory: String? = null,
    val color: String,
    val size: String,
    val season: List<Season>,
    val imageUrl: String? = null,
    val notes: String? = null,
    val liked: Boolean = false,
    val wearCount: Int = 0,
    val userId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

enum class ClothingCategory(val displayName: String) {
    TOPS("Tops"),
    BOTTOMS("Bottoms"), 
    DRESSES("Dresses"),
    OUTERWEAR("Outerwear"),
    SHOES("Shoes"),
    ACCESSORIES("Accessories"),
    OTHER("Other")
}

enum class Season(val displayName: String) {
    SPRING("Spring"),
    SUMMER("Summer"),
    FALL("Fall"),
    WINTER("Winter"),
    ALL("All")
}