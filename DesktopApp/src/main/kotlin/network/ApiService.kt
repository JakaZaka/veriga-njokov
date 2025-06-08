package network

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import models.ApiResponse

/**
 * Service for making API requests to the backend
 */
class ApiService(private val client: HttpClient) {
    
    // Generic GET method
    suspend fun <T> get(url: String, responseParser: suspend (String) -> T): ApiResponse<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.get<HttpResponse>(url)
                if (response.status.isSuccess()) {
                    val text = response.readText()
                    val data = responseParser(text)
                    ApiResponse(success = true, data = data)
                } else {
                    ApiResponse(success = false, error = "Error: ${response.status}")
                }
            } catch (e: Exception) {
                ApiResponse(success = false, error = e.message)
            }
        }
    }
    
    // Generic POST method
    suspend fun <T> post(url: String, body: Any, responseParser: suspend (String) -> T): ApiResponse<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.post<HttpResponse> {
                    url(url)
                    contentType(ContentType.Application.Json)
                    this.body = body
                }
                
                if (response.status.isSuccess()) {
                    val text = response.readText()
                    val data = responseParser(text)
                    ApiResponse(success = true, data = data)
                } else {
                    ApiResponse(success = false, error = "Error: ${response.status}")
                }
            } catch (e: Exception) {
                ApiResponse(success = false, error = e.message)
            }
        }
    }
    
    // Generic PUT method
    suspend fun <T> put(url: String, body: Any, responseParser: suspend (String) -> T): ApiResponse<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.put<HttpResponse> {
                    url(url)
                    contentType(ContentType.Application.Json)
                    this.body = body
                }
                
                if (response.status.isSuccess()) {
                    val text = response.readText()
                    val data = responseParser(text)
                    ApiResponse(success = true, data = data)
                } else {
                    ApiResponse(success = false, error = "Error: ${response.status}")
                }
            } catch (e: Exception) {
                ApiResponse(success = false, error = e.message)
            }
        }
    }
    
    // Generic DELETE method
    suspend fun delete(url: String): ApiResponse<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = client.delete<HttpResponse>(url)
                ApiResponse(success = response.status.isSuccess(), data = response.status.isSuccess())
            } catch (e: Exception) {
                ApiResponse(success = false, error = e.message)
            }
        }
    }
}
