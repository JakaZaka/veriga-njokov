package models

data class ClothingItem(
    val id: String = "",
    val fromShop: Boolean = false,
    val clothingStoreId: String? = null,
    val userId: String? = null,
    val name: String,
    val category: ClothingCategory,
    val subCategory: String? = null,
    val color: String? = null,
    val size: String? = null,
    val season: List<Season> = emptyList(),
    val wantToGet: Boolean = false,
    val liked: Boolean = false,
    val wantToGive: Boolean = false,
    val imageUrl: String? = null,
    val notes: String? = null,
    val price: Double? = null,
    val metadata: Map<String, Any> = emptyMap()
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