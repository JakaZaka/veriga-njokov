package com.example.closy.model

import com.google.gson.annotations.SerializedName

/**
 * Base data class for all sensor readings
 */
data class SensorData(
    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("sensor_type")
    val sensorType: String,

    @SerializedName("location")
    val location: LocationData?,

    @SerializedName("data")
    val data: Map<String, Any>
)

/**
 * Location data captured with each sensor reading
 */
data class LocationData(
    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("altitude")
    val altitude: Double?,

    @SerializedName("accuracy")
    val accuracy: Float?
)

/**
 * Accelerometer sensor data
 */
data class AccelerometerData(
    val x: Float,
    val y: Float,
    val z: Float
)

/**
 * Gyroscope sensor data
 */
data class GyroscopeData(
    val x: Float,
    val y: Float,
    val z: Float
)

/**
 * Light sensor data
 */
data class LightData(
    val lux: Float
)

/**
 * Proximity sensor data
 */
data class ProximityData(
    val distance: Float
)

/**
 * Temperature sensor data
 */
data class TemperatureData(
    val celsius: Float
)

/**
 * Audio data
 */
data class AudioData(
    val amplitude: Double,
    val filePath: String?
)

/**
 * Camera/Image data
 */
data class CameraData(
    val imagePath: String,
    val width: Int,
    val height: Int
)

/**
 * Sensor configuration
 */
data class SensorConfig(
    val sensorType: SensorType,
    var isEnabled: Boolean = false,
    var samplingIntervalSeconds: Int = 60 // Default: every 60 seconds
)

/**
 * Enum for supported sensor types
 */
enum class SensorType {
    ACCELEROMETER,
    GYROSCOPE,
    LIGHT,
    PROXIMITY,
    TEMPERATURE,
    MICROPHONE,
    CAMERA,
    GPS
}

