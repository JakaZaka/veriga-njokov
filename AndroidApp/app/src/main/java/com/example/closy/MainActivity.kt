package com.example.closy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.closy.model.SensorType
import com.example.closy.network.NetworkManager
import com.example.closy.sensors.SensorDataManager
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var sensorDataManager: SensorDataManager
    private lateinit var networkManager: NetworkManager

    private lateinit var serverUrlInput: TextInputEditText
    private lateinit var statusText: TextView

    // Sensor UI components mapped by SensorType
    private val sensorViews = mutableMapOf<SensorType, SensorItemViews>()

    // Required permissions
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            updateStatus("All permissions granted")
            initializeSensors()
        } else {
            updateStatus("Some permissions denied. App may not function properly.")
            Toast.makeText(
                this,
                "Please grant all permissions for full functionality",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        serverUrlInput = findViewById(R.id.serverUrlInput)
        statusText = findViewById(R.id.statusText)

        // Initialize network manager
        val serverUrl = serverUrlInput.text.toString()
        networkManager = NetworkManager(serverUrl)

        // Initialize sensor data manager
        sensorDataManager = SensorDataManager(this, networkManager)

        // Setup sensor UI components
        setupSensorUI()

        // Setup Event Publisher button
        findViewById<Button>(R.id.openEventPublisherButton).setOnClickListener {
            openEventPublisher()
        }

        // Request permissions
        checkAndRequestPermissions()
    }

    private fun openEventPublisher() {
        val intent = Intent(this, EventPublisherActivity::class.java)
        startActivity(intent)
    }

    private fun setupSensorUI() {
        // Map sensor types to their UI components
        val sensorConfigs = listOf(
            Triple(R.id.accelerometerItem, SensorType.ACCELEROMETER, "Accelerometer"),
            Triple(R.id.gyroscopeItem, SensorType.GYROSCOPE, "Gyroscope"),
            Triple(R.id.lightItem, SensorType.LIGHT, "Light Sensor"),
            Triple(R.id.proximityItem, SensorType.PROXIMITY, "Proximity"),
            Triple(R.id.gpsItem, SensorType.GPS, "GPS Location"),
            Triple(R.id.cameraItem, SensorType.CAMERA, "Camera"),
            Triple(R.id.microphoneItem, SensorType.MICROPHONE, "Microphone")
        )

        for ((viewId, sensorType, name) in sensorConfigs) {
            val itemView = findViewById<View>(viewId)
            val sensorName = itemView.findViewById<TextView>(R.id.sensorName)
            val sensorSwitch = itemView.findViewById<SwitchMaterial>(R.id.sensorSwitch)
            val configLayout = itemView.findViewById<View>(R.id.configLayout)
            val intervalInput = itemView.findViewById<EditText>(R.id.intervalInput)
            val sensorStatus = itemView.findViewById<TextView>(R.id.sensorStatus)

            sensorName.text = name

            // Store views for later access
            sensorViews[sensorType] = SensorItemViews(
                sensorSwitch,
                configLayout,
                intervalInput,
                sensorStatus
            )

            // Check if sensor is available
            val isAvailable = sensorDataManager.isSensorAvailable(sensorType)
            if (!isAvailable) {
                sensorSwitch.isEnabled = false
                sensorStatus.text = "Status: Not available"
            }

            // Setup switch listener
            sensorSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    startSensor(sensorType)
                } else {
                    stopSensor(sensorType)
                }
                configLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
            }

            // Setup interval update
            intervalInput.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    updateSensorInterval(sensorType)
                }
            }
        }
    }

    private fun initializeSensors() {
        // Update sensor availability status
        sensorViews.forEach { (sensorType, views) ->
            val isAvailable = sensorDataManager.isSensorAvailable(sensorType)
            if (!isAvailable) {
                views.sensorSwitch.isEnabled = false
                views.sensorStatus.text = "Status: Not available"
            } else {
                views.sensorStatus.text = "Status: Ready"
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            initializeSensors()
        }
    }

    private fun startSensor(sensorType: SensorType) {
        val views = sensorViews[sensorType] ?: return

        try {
            sensorDataManager.startSensor(sensorType)
            views.sensorStatus.text = "Status: Active"
            updateStatus("Started ${sensorType.name} sensor")
        } catch (e: Exception) {
            views.sensorStatus.text = "Status: Error"
            views.sensorSwitch.isChecked = false
            updateStatus("Error starting ${sensorType.name}: ${e.message}")
            Toast.makeText(this, "Error starting sensor: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopSensor(sensorType: SensorType) {
        val views = sensorViews[sensorType] ?: return

        try {
            sensorDataManager.stopSensor(sensorType)
            views.sensorStatus.text = "Status: Inactive"
            updateStatus("Stopped ${sensorType.name} sensor")
        } catch (e: Exception) {
            updateStatus("Error stopping ${sensorType.name}: ${e.message}")
        }
    }

    private fun updateSensorInterval(sensorType: SensorType) {
        val views = sensorViews[sensorType] ?: return
        val intervalText = views.intervalInput.text.toString()

        try {
            val interval = intervalText.toIntOrNull() ?: 60
            if (interval < 1) {
                Toast.makeText(this, "Interval must be at least 1 second", Toast.LENGTH_SHORT).show()
                views.intervalInput.setText("60")
                return
            }

            sensorDataManager.updateSensorConfig(sensorType, interval)
            updateStatus("Updated ${sensorType.name} interval to $interval seconds")
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid interval value", Toast.LENGTH_SHORT).show()
            views.intervalInput.setText("60")
        }
    }

    private fun updateStatus(message: String) {
        runOnUiThread {
            statusText.text = "$message\n${statusText.text}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorDataManager.cleanup()
    }

    /**
     * Data class to hold sensor item views
     */
    private data class SensorItemViews(
        val sensorSwitch: SwitchMaterial,
        val configLayout: View,
        val intervalInput: EditText,
        val sensorStatus: TextView
    )
}

