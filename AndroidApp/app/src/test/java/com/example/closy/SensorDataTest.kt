package com.example.closy

import com.example.closy.model.*
import com.google.gson.Gson
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Closy application
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class SensorDataTest {

    private val gson = Gson()

    @Test
    fun testSensorDataSerialization() {
        // Create test data
        val location = LocationData(
            latitude = 46.5547,
            longitude = 15.6466,
            altitude = 275.0,
            accuracy = 10.5f
        )

        val sensorData = SensorData(
            timestamp = 1704326400000L,
            sensorType = "ACCELEROMETER",
            location = location,
            data = mapOf("x" to 0.123, "y" to 9.81, "z" to 0.456)
        )

        // Serialize to JSON
        val json = gson.toJson(sensorData)

        // Verify JSON is not empty
        assertNotNull(json)
        assertTrue(json.isNotEmpty())

        // Deserialize back
        val deserialized = gson.fromJson(json, SensorData::class.java)

        // Verify data integrity
        assertEquals(sensorData.timestamp, deserialized.timestamp)
        assertEquals(sensorData.sensorType, deserialized.sensorType)
        assertEquals(sensorData.location?.latitude, deserialized.location?.latitude)
        assertEquals(sensorData.location?.longitude, deserialized.location?.longitude)
    }

    @Test
    fun testAccelerometerData() {
        val data = AccelerometerData(x = 0.1f, y = 9.8f, z = 0.2f)

        assertNotNull(data)
        assertEquals(0.1f, data.x, 0.001f)
        assertEquals(9.8f, data.y, 0.001f)
        assertEquals(0.2f, data.z, 0.001f)
    }

    @Test
    fun testGyroscopeData() {
        val data = GyroscopeData(x = 0.01f, y = 0.02f, z = 0.03f)

        assertNotNull(data)
        assertEquals(0.01f, data.x, 0.0001f)
        assertEquals(0.02f, data.y, 0.0001f)
        assertEquals(0.03f, data.z, 0.0001f)
    }

    @Test
    fun testLightData() {
        val data = LightData(lux = 250.5f)

        assertNotNull(data)
        assertEquals(250.5f, data.lux, 0.001f)
    }

    @Test
    fun testLocationData() {
        val location = LocationData(
            latitude = 46.5547,
            longitude = 15.6466,
            altitude = 275.0,
            accuracy = 10.5f
        )

        assertNotNull(location)
        assertEquals(46.5547, location.latitude, 0.0001)
        assertEquals(15.6466, location.longitude, 0.0001)
        assertEquals(275.0, location.altitude!!, 0.1)
        assertEquals(10.5f, location.accuracy!!, 0.1f)
    }

    @Test
    fun testSensorConfig() {
        val config = SensorConfig(
            sensorType = SensorType.ACCELEROMETER,
            isEnabled = true,
            samplingIntervalSeconds = 120
        )

        assertNotNull(config)
        assertEquals(SensorType.ACCELEROMETER, config.sensorType)
        assertTrue(config.isEnabled)
        assertEquals(120, config.samplingIntervalSeconds)
    }

    @Test
    fun testSensorTypeEnum() {
        val types = SensorType.values()

        assertTrue(types.contains(SensorType.ACCELEROMETER))
        assertTrue(types.contains(SensorType.GYROSCOPE))
        assertTrue(types.contains(SensorType.LIGHT))
        assertTrue(types.contains(SensorType.PROXIMITY))
        assertTrue(types.contains(SensorType.GPS))
        assertTrue(types.contains(SensorType.CAMERA))
        assertTrue(types.contains(SensorType.MICROPHONE))
    }

    @Test
    fun testSensorDataWithNullLocation() {
        val sensorData = SensorData(
            timestamp = System.currentTimeMillis(),
            sensorType = "TEST",
            location = null,
            data = mapOf("test" to "value")
        )

        assertNotNull(sensorData)
        assertNull(sensorData.location)
    }

    @Test
    fun testJsonSerializationFormat() {
        val sensorData = SensorData(
            timestamp = 1704326400000L,
            sensorType = "ACCELEROMETER",
            location = LocationData(46.5547, 15.6466, null, null),
            data = mapOf("x" to 0.1, "y" to 0.2, "z" to 0.3)
        )

        val json = gson.toJson(sensorData)

        // Verify JSON contains expected fields
        assertTrue(json.contains("\"timestamp\""))
        assertTrue(json.contains("\"sensor_type\""))
        assertTrue(json.contains("\"location\""))
        assertTrue(json.contains("\"data\""))
        assertTrue(json.contains("\"latitude\""))
        assertTrue(json.contains("\"longitude\""))
    }

    @Test
    fun testAudioData() {
        val audioData = AudioData(
            amplitude = 1234.56,
            filePath = "/storage/audio.3gp"
        )

        assertNotNull(audioData)
        assertEquals(1234.56, audioData.amplitude, 0.01)
        assertEquals("/storage/audio.3gp", audioData.filePath)
    }

    @Test
    fun testCameraData() {
        val cameraData = CameraData(
            imagePath = "/storage/image.jpg",
            width = 1920,
            height = 1080
        )

        assertNotNull(cameraData)
        assertEquals("/storage/image.jpg", cameraData.imagePath)
        assertEquals(1920, cameraData.width)
        assertEquals(1080, cameraData.height)
    }

    @Test
    fun testProximityData() {
        val proximityData = ProximityData(distance = 5.0f)

        assertNotNull(proximityData)
        assertEquals(5.0f, proximityData.distance, 0.001f)
    }

    @Test
    fun testTemperatureData() {
        val temperatureData = TemperatureData(celsius = 22.5f)

        assertNotNull(temperatureData)
        assertEquals(22.5f, temperatureData.celsius, 0.001f)
    }
}

