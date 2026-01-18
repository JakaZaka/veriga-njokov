package com.example.closy.network

import com.example.closy.model.SensorData
import com.example.closy.model.DigitalTwinEvent
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Network manager for sending sensor data and events to server using OkHttp
 */
class NetworkManager(private val serverUrl: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    // Extract base URL (remove /api/sensor-data if present)
    private val baseUrl: String = serverUrl.replace("/api/sensor-data", "").replace("/api", "")
    private val eventsUrl: String = "$baseUrl/api/events"

    /**
     * Send sensor data to server
     */
    fun sendSensorData(sensorData: SensorData, callback: (Boolean, String?) -> Unit) {
        try {
            val json = gson.toJson(sensorData)
            val body = json.toRequestBody(JSON)

            val request = Request.Builder()
                .url(serverUrl)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    callback(true, response.body?.string())
                } else {
                    callback(false, "Error: ${response.code} - ${response.message}")
                }
            }
        } catch (e: Exception) {
            callback(false, "Exception: ${e.message}")
        }
    }

    /**
     * Send batch of sensor data to server
     */
    fun sendBatchData(sensorDataList: List<SensorData>, callback: (Boolean, String?) -> Unit) {
        try {
            val json = gson.toJson(sensorDataList)
            val body = json.toRequestBody(JSON)

            val request = Request.Builder()
                .url("$serverUrl/batch")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    callback(true, response.body?.string())
                } else {
                    callback(false, "Error: ${response.code} - ${response.message}")
                }
            }
        } catch (e: Exception) {
            callback(false, "Exception: ${e.message}")
        }
    }

    /**
     * Send Digital Twin Event to server
     */
    fun sendEventData(event: DigitalTwinEvent, callback: (Boolean, String?) -> Unit) {
        try {
            val json = gson.toJson(event)
            val body = json.toRequestBody(JSON)

            println("NetworkManager: Sending event to: $eventsUrl")
            println("NetworkManager: Event JSON: $json")

            val request = Request.Builder()
                .url(eventsUrl)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                println("NetworkManager: Response code: ${response.code}")
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    println("NetworkManager: Success response: $responseBody")
                    callback(true, responseBody)
                } else {
                    val errorBody = response.body?.string()
                    println("NetworkManager: Error response: $errorBody")
                    callback(false, "Error: ${response.code} - ${response.message} - $errorBody")
                }
            }
        } catch (e: Exception) {
            println("NetworkManager: Exception: ${e.message}")
            e.printStackTrace()
            callback(false, "Exception: ${e.message}")
        }
    }

    /**
     * Send batch of events to server
     */
    fun sendBatchEvents(events: List<DigitalTwinEvent>, callback: (Boolean, String?) -> Unit) {
        try {
            val json = gson.toJson(events)
            val body = json.toRequestBody(JSON)

            val request = Request.Builder()
                .url("$eventsUrl/batch")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    callback(true, response.body?.string())
                } else {
                    callback(false, "Error: ${response.code} - ${response.message}")
                }
            }
        } catch (e: Exception) {
            callback(false, "Exception: ${e.message}")
        }
    }
}

