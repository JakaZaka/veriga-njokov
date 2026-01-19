package com.example.closy.model

import com.google.gson.annotations.SerializedName

data class LocationData(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("altitude") val altitude: Double?,
    @SerializedName("accuracy") val accuracy: Double?
)
