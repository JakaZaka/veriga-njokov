package models

import kotlinx.serialization.Serializable

@Serializable
data class Outfit(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    val items: List<String>,
    val owner: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)