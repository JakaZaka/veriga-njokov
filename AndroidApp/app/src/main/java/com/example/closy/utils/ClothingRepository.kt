package com.example.closy.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import com.example.closy.BuildConfig

class ClothingRepository {

    /**
     * AI Klasifikacija - Port 5001
     */
    suspend fun classifyImage(imageUri: Uri, context: Context): Result<ClassificationData> {
        return try {
            val prefs = context.getSharedPreferences("ClosyPreferences", Context.MODE_PRIVATE)
            val savedUrl = prefs.getString("server_url", "") ?: ""

            // Dinamično dobimo IP iz vpisanega URL-ja
            val baseIp = if (savedUrl.contains("://")) {
                savedUrl.substringAfter("://").substringBefore(":")
            } else {
                "192.168.1.14" // Fallback na tvoj zadnji znani IP
            }

            val aiBaseUrl = "http://$baseIp:5001/"
            val aiApi = RetrofitClient.getApi(aiBaseUrl)

            val file = uriToFile(imageUri, context)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val response = aiApi.classifyClothing(imagePart)
            file.delete()

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) Result.success(data)
                else Result.failure(Exception("No data from AI"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "AI Classification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Shranjevanje v bazo - Port 5000
     */
    suspend fun createClothingWithAI(
        imageUri: Uri,
        context: Context,
        token: String,
        overrides: ClothingItemOverrides? = null
    ): Result<CreateClothingData> {
        return try {
            val prefs = context.getSharedPreferences("ClosyPreferences", Context.MODE_PRIVATE)
            val savedUrl = prefs.getString("server_url", "")

            val finalUrl = if (savedUrl.isNullOrBlank()) {
                "http://192.168.1.14:5000/api"
            } else {
                savedUrl
            }

            // Očistimo URL za Retrofit (npr. http://192.168.1.14:5000/)
            val mainBaseUrl = try {
                val uri = android.net.Uri.parse(finalUrl)
                "${uri.scheme}://${uri.authority}/"
            } catch (e: Exception) {
                finalUrl.substringBefore("/api").let { if (it.endsWith("/")) it else "$it/" }
            }

            val mainApi = RetrofitClient.getApi(mainBaseUrl)

            val file = uriToFile(imageUri, context)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

            // PRIPRAVA PODATKOV (Season fix!)
            val nameBody = overrides?.name?.toRequestBody("text/plain".toMediaTypeOrNull())
            val categoryBody = overrides?.category?.toRequestBody("text/plain".toMediaTypeOrNull())
            val subCategoryBody = overrides?.subCategory?.toRequestBody("text/plain".toMediaTypeOrNull())
            val colorBody = overrides?.color?.toRequestBody("text/plain".toMediaTypeOrNull())
            val sizeBody = overrides?.size?.toRequestBody("text/plain".toMediaTypeOrNull())
            val notesBody = overrides?.notes?.toRequestBody("text/plain".toMediaTypeOrNull())

            // REŠITEV ZA SEASONS:
            // Node.js/Multer pričakuje polje kot več parametrov z istim imenom "season"
            // Vendar ker ClothingApiService pričakuje RequestBody?, bomo poslali vejice,
            // vendar v kontrolerju na backendu (clothingController.js) dodaj:
            // if (typeof req.body.season === 'string') req.body.season = req.body.season.split(',')
            val seasonBody = overrides?.season?.joinToString(",")?.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = mainApi.createClothingWithAI(
                token = "Bearer $token",
                image = imagePart,
                name = nameBody,
                category = categoryBody,
                subCategory = subCategoryBody,
                color = colorBody,
                size = sizeBody,
                season = seasonBody,
                notes = notesBody
            )

            file.delete()

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) Result.success(data)
                else Result.failure(Exception("No data in response"))
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.body()?.message ?: "Unknown error"
                Result.failure(Exception("Glavni Server Napaka: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateClothingItem(id: String, token: String, item: ClothingItem): Result<ClothingItem> {
        return try {
            val response = RetrofitClient.api.updateClothingItem("Bearer $token", id, item)
            if (response.isSuccessful && response.body()?.success == true) {
                val updatedItem = response.body()?.data?.clothingItem
                if (updatedItem != null) Result.success(updatedItem)
                else Result.failure(Exception("No data in response"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Update failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }
}

data class ClothingItemOverrides(
    val name: String? = null,
    val category: String? = null,
    val subCategory: String? = null,
    val color: String? = null,
    val size: String? = null,
    val season: List<String>? = null,
    val notes: String? = null
)