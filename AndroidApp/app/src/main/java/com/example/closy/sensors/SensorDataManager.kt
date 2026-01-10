package com.example.closy.sensors

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.closy.model.*
import com.example.closy.network.NetworkManager
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Main sensor manager that handles all sensor data collection
 */
class SensorDataManager(
    private val context: Context,
    private val networkManager: NetworkManager
) {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val locationProvider = LocationProvider(context)

    // Store latest sensor values
    private val sensorValues = ConcurrentHashMap<SensorType, Any>()

    // Sensor configurations
    private val sensorConfigs = ConcurrentHashMap<SensorType, SensorConfig>()

    // Coroutine scope for sensor data collection
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Jobs for periodic data collection
    private val sensorJobs = ConcurrentHashMap<SensorType, Job>()

    init {
        // Initialize sensor configurations
        SensorType.values().forEach { type ->
            sensorConfigs[type] = SensorConfig(type)
        }
    }

    /**
     * Accelerometer sensor listener
     */
    private val accelerometerListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val data = AccelerometerData(it.values[0], it.values[1], it.values[2])
                sensorValues[SensorType.ACCELEROMETER] = data
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    /**
     * Gyroscope sensor listener
     */
    private val gyroscopeListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val data = GyroscopeData(it.values[0], it.values[1], it.values[2])
                sensorValues[SensorType.GYROSCOPE] = data
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    /**
     * Light sensor listener
     */
    private val lightListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val data = LightData(it.values[0])
                sensorValues[SensorType.LIGHT] = data
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    /**
     * Proximity sensor listener
     */
    private val proximityListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val data = ProximityData(it.values[0])
                sensorValues[SensorType.PROXIMITY] = data
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    /**
     * Temperature sensor listener
     */
    private val temperatureListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val data = TemperatureData(it.values[0])
                sensorValues[SensorType.TEMPERATURE] = data
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    /**
     * Start sensor data collection
     */
    fun startSensor(sensorType: SensorType) {
        val config = sensorConfigs[sensorType] ?: return
        config.isEnabled = true

        when (sensorType) {
            SensorType.ACCELEROMETER -> {
                val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                sensor?.let {
                    sensorManager.registerListener(
                        accelerometerListener,
                        it,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                }
            }
            SensorType.GYROSCOPE -> {
                val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
                sensor?.let {
                    sensorManager.registerListener(
                        gyroscopeListener,
                        it,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                }
            }
            SensorType.LIGHT -> {
                val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
                sensor?.let {
                    sensorManager.registerListener(
                        lightListener,
                        it,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                }
            }
            SensorType.PROXIMITY -> {
                val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
                sensor?.let {
                    sensorManager.registerListener(
                        proximityListener,
                        it,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                }
            }
            SensorType.TEMPERATURE -> {
                val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
                sensor?.let {
                    sensorManager.registerListener(
                        temperatureListener,
                        it,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                }
            }
            SensorType.GPS -> {
                locationProvider.startLocationUpdates()
            }
            else -> {
                // Camera and Microphone handled separately
            }
        }

        // Start periodic data upload
        startPeriodicUpload(sensorType)
    }

    /**
     * Stop sensor data collection
     */
    fun stopSensor(sensorType: SensorType) {
        val config = sensorConfigs[sensorType] ?: return
        config.isEnabled = false

        when (sensorType) {
            SensorType.ACCELEROMETER -> sensorManager.unregisterListener(accelerometerListener)
            SensorType.GYROSCOPE -> sensorManager.unregisterListener(gyroscopeListener)
            SensorType.LIGHT -> sensorManager.unregisterListener(lightListener)
            SensorType.PROXIMITY -> sensorManager.unregisterListener(proximityListener)
            SensorType.TEMPERATURE -> sensorManager.unregisterListener(temperatureListener)
            SensorType.GPS -> locationProvider.stopLocationUpdates()
            else -> {}
        }

        // Cancel periodic upload job
        sensorJobs[sensorType]?.cancel()
        sensorJobs.remove(sensorType)
    }

    /**
     * Start periodic data upload
     */
    private fun startPeriodicUpload(sensorType: SensorType) {
        val config = sensorConfigs[sensorType] ?: return

        sensorJobs[sensorType]?.cancel()

        val job = scope.launch {
            while (isActive && config.isEnabled) {
                delay(config.samplingIntervalSeconds * 1000L)

                // Collect and send data
                val sensorData = collectSensorData(sensorType)
                sensorData?.let { data ->
                    withContext(Dispatchers.IO) {
                        networkManager.sendSensorData(data) { success, message ->
                            // Handle response
                            if (!success) {
                                println("Failed to send data: $message")
                            }
                        }
                    }
                }
            }
        }

        sensorJobs[sensorType] = job
    }

    /**
     * Collect data from a specific sensor
     */
    private fun collectSensorData(sensorType: SensorType): SensorData? {
        val value = sensorValues[sensorType] ?: return null
        val location = locationProvider.getCurrentLocation()

        val dataMap = when (value) {
            is AccelerometerData -> mapOf("x" to value.x, "y" to value.y, "z" to value.z)
            is GyroscopeData -> mapOf("x" to value.x, "y" to value.y, "z" to value.z)
            is LightData -> mapOf("lux" to value.lux)
            is ProximityData -> mapOf("distance" to value.distance)
            is TemperatureData -> mapOf("celsius" to value.celsius)
            is AudioData -> mapOf("amplitude" to value.amplitude, "filePath" to (value.filePath ?: ""))
            is CameraData -> mapOf("imagePath" to value.imagePath, "width" to value.width, "height" to value.height)
            else -> return null
        }

        return SensorData(
            timestamp = System.currentTimeMillis(),
            sensorType = sensorType.name,
            location = location,
            data = dataMap
        )
    }

    /**
     * Update sensor configuration
     */
    fun updateSensorConfig(sensorType: SensorType, intervalSeconds: Int) {
        sensorConfigs[sensorType]?.samplingIntervalSeconds = intervalSeconds

        // Restart sensor if it's currently running
        if (sensorConfigs[sensorType]?.isEnabled == true) {
            stopSensor(sensorType)
            startSensor(sensorType)
        }
    }

    /**
     * Get sensor configuration
     */
    fun getSensorConfig(sensorType: SensorType): SensorConfig? {
        return sensorConfigs[sensorType]
    }

    /**
     * Check if sensor is available
     */
    fun isSensorAvailable(sensorType: SensorType): Boolean {
        return when (sensorType) {
            SensorType.ACCELEROMETER -> sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
            SensorType.GYROSCOPE -> sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
            SensorType.LIGHT -> sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null
            SensorType.PROXIMITY -> sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null
            SensorType.TEMPERATURE -> sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null
            SensorType.GPS -> true
            SensorType.CAMERA -> context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
            SensorType.MICROPHONE -> true
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        SensorType.values().forEach { stopSensor(it) }
        locationProvider.stopLocationUpdates()
        scope.cancel()
    }
}

