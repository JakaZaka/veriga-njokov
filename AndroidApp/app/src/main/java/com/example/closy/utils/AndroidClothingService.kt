package com.example.closy.utils

import com.google.gson.annotations.SerializedName

data class ColorInfo(
    @SerializedName("name") val name: String,
    @SerializedName("name_sl") val nameSl: String,
    @SerializedName("rgb") val rgb: List<Int>,
    @SerializedName("percentage") val percentage: Double
)

data class Top5Prediction(
    @SerializedName("label") val label: String,
    @SerializedName("confidence") val confidence: Double
)

data class ClassificationData(
    @SerializedName("classification") val classification: String,
    @SerializedName("category") val category: String,
    @SerializedName("subCategory") val subCategory: String,
    @SerializedName("confidence") val confidence: Double,
    @SerializedName("colors") val colors: List<ColorInfo>,
    @SerializedName("primaryColor") val primaryColor: String,
    @SerializedName("top5") val top5: List<Top5Prediction>
)

data class ClassificationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ClassificationData?,
    @SerializedName("error") val error: String?
)

data class ClothingItem(
    @SerializedName("_id") val id: String?,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("subCategory") val subCategory: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("size") val size: String?,
    @SerializedName("season") val season: List<String>?,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("user") val user: String?
)

data class CreateClothingResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: CreateClothingData?,
    @SerializedName("message") val message: String?
)

data class CreateClothingData(
    @SerializedName("clothingItem") val clothingItem: ClothingItem,
    @SerializedName("aiResults") val aiResults: ClassificationData?
)


