package models

import kotlinx.serialization.Serializable

@Serializable
data class Weather(
    val id: String? = null,
    val location: String,
    val temperature: Double,
    val isRaining: Boolean = false,
    val isSnowing: Boolean = false,
    val fetchedAt: Long? = System.currentTimeMillis(),
    val notes: String? = null
)
