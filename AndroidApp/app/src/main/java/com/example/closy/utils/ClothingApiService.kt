package com.example.closy.utils

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ClothingApiService {

    /**
     * Klasificiraj sliko (samo AI, ne shrani)
     */
    @Multipart
    @POST("api/clothing/classify")
    suspend fun classifyClothing(
        @Part image: MultipartBody.Part
    ): Response<ClassificationResponse>

    /**
     * Ustvari clothing item z AI klasifikacijo
     */
    @Multipart
    @POST("api/clothing/create-with-ai")
    suspend fun createClothingWithAI(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part,
        @Part("name") name: RequestBody?,
        @Part("category") category: RequestBody?,
        @Part("subCategory") subCategory: RequestBody?,
        @Part("color") color: RequestBody?,
        @Part("size") size: RequestBody?,
        @Part("season") season: RequestBody?,
        @Part("notes") notes: RequestBody?
    ): Response<CreateClothingResponse>

    /**
     * Posodobi clothing item
     */
    @PUT("api/clothing/{id}")
    suspend fun updateClothingItem(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body item: ClothingItem
    ): Response<CreateClothingResponse>
}
