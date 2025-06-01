package models

data class Weather(
    val id: String = "",
    val location: String,
    val temperature: Double,
    val isRaining: Boolean = false,
    val isSnowing: Boolean = false,
    val fetchedAt: Long = System.currentTimeMillis()
)