package models

data class ClothingStore(
    val id: String = "",
    val name: String,
    val website: String? = null,
    val locations: List<Location> = emptyList()
)