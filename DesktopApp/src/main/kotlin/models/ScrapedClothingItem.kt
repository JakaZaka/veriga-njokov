package models

import kotlinx.serialization.Serializable

@Serializable
data class ScrapedClothingItem(
    val name: String,
    val category: String,
    val imageUrl: String,
    val color: String,
    val link: String,
    var selected: Boolean = false, // For UI selection tracking
    
    // Additional fields for editing - use String for seasons instead of Season enum
    var size: String = "M",
    var seasons: List<String> = listOf("summer"),
    var notes: String? = null
)