package models

data class Outfit(
    val id: String = "",
    val userId: String,
    val name: String,
    val items: List<OutfitItem> = emptyList(),
    val season: List<Season> = emptyList(),
    val occasion: String? = null,
    val liked: Int = 0,
    val likedBy: List<String> = emptyList(),
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class OutfitItem(
    val itemId: String,
    val position: ClothingPosition
)

enum class ClothingPosition(val displayName: String) {
    TOP("Top"),
    BOTTOM("Bottom"),
    OUTER("Outer"),
    SHOES("Shoes"),
    ACCESSORY("Accessory"),
    OTHER("Other")
}