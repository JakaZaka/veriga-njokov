package models

data class WeatherRecommendation(
    val weatherMessage: String,
    val recommendedItems: List<ClothingItem> = emptyList()
)