package com.example.closy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.closy.model.SensorType
import com.example.closy.network.NetworkManager
import com.example.closy.sensors.SensorDataManager
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * Activity for managing individual sensor data collection
 * Each sensor can be toggled on/off with customizable intervals
 */
class SensorsActivity : AppCompatActivity() {

    private lateinit var sensorDataManager: SensorDataManager
    private lateinit var networkManager: NetworkManager

    // Sensor UI components mapped by SensorType
    private val sensorViews = mutableMapOf<SensorType, SensorCardViews>()

    // Required permissions
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            initializeSensors()
        } else {
            Toast.makeText(
                this,
                "Some permissions denied. App may not function properly.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensors)

        // Setup back button
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Get server URL from shared preferences
        val serverUrl = getSharedPreferences("ClosyPreferences", MODE_PRIVATE)
            .getString("server_url", "http://10.0.2.2:5000/api") ?: "http://10.0.2.2:5000/api"

        // Initialize managers
        networkManager = NetworkManager(serverUrl)
        sensorDataManager = SensorDataManager(this, networkManager)

        // Setup sensor UI
        setupSensorCards()

        // Check and request permissions
        checkAndRequestPermissions()
    }

    private fun setupSensorCards() {
        val sensorConfigs = listOf(
            Triple(R.id.gpsCard, SensorType.GPS, "GPS Location"),
            Triple(R.id.cameraCard, SensorType.CAMERA, "Camera")
        )

        for ((cardId, sensorType, name) in sensorConfigs) {
            val card = findViewById<View>(cardId)
            if (sensorType == SensorType.GPS) {
                val sensorName = card.findViewById<TextView>(R.id.sensorNameText)
                val sensorSwitch = card.findViewById<SwitchMaterial>(R.id.sensorSwitch)
                val intervalSpinner = card.findViewById<Spinner>(R.id.intervalSpinner)
                sensorName.text = name
                setupIntervalSpinner(intervalSpinner)
                sensorViews[sensorType] = SensorCardViews(sensorSwitch, intervalSpinner, null, null, null)
                val isAvailable = sensorDataManager.isSensorAvailable(sensorType)
                sensorSwitch.isEnabled = isAvailable
                sensorSwitch.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) startSensor(sensorType) else stopSensor(sensorType)
                }
                intervalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (sensorSwitch.isChecked) updateSensorInterval(sensorType, position)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            } else if (sensorType == SensorType.CAMERA) {
                val sensorName = card.findViewById<TextView>(R.id.sensorNameText)
                val openCameraButton = card.findViewById<Button>(R.id.openCameraButton)
                sensorName.text = name
                openCameraButton.setOnClickListener {
                    val intent = Intent(this, CameraActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun setupIntervalSpinner(spinner: Spinner) {
        val intervals = arrayOf("1s", "5s", "10s", "30s", "1m", "5m", "10m")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, intervals)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(2) // Default to 10s
    }

    private fun getIntervalSeconds(position: Int): Int {
        return when (position) {
            0 -> 1      // 1s
            1 -> 5      // 5s
            2 -> 10     // 10s
            3 -> 30     // 30s
            4 -> 60     // 1m
            5 -> 300    // 5m
            6 -> 600    // 10m
            else -> 10
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

    private fun initializeSensors() {
        sensorViews.forEach { (sensorType, views) ->
            val isAvailable = sensorDataManager.isSensorAvailable(sensorType)
            views.statusText?.let {
                if (isAvailable) {
                    it.text = "Ready"
                    it.setTextColor(getColor(android.R.color.darker_gray))
                }
            }
        }
    }

    private fun startSensor(sensorType: SensorType) {
        val views = sensorViews[sensorType] ?: return
        val intervalSeconds = views.intervalSpinner.selectedItemPosition.let { getIntervalSeconds(it) }

        try {
            sensorDataManager.updateSensorConfig(sensorType, intervalSeconds)
            sensorDataManager.startSensor(sensorType)

            views.statusText?.let {
                it.text = "● Active"
                it.setTextColor(getColor(android.R.color.holo_green_dark))
            }

            Toast.makeText(this, "${sensorType.name} started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            views.statusText?.let {
                it.text = "Error: ${e.message}"
                it.setTextColor(getColor(android.R.color.holo_red_dark))
            }
            views.sensorSwitch.isChecked = false
            Toast.makeText(this, "Error starting sensor: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopSensor(sensorType: SensorType) {
        val views = sensorViews[sensorType] ?: return

        try {
            sensorDataManager.stopSensor(sensorType)
            views.statusText?.let {
                it.text = "Inactive"
                it.setTextColor(getColor(android.R.color.darker_gray))
            }
            Toast.makeText(this, "${sensorType.name} stopped", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error stopping sensor: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSensorInterval(sensorType: SensorType, spinnerPosition: Int) {
        val intervalSeconds = getIntervalSeconds(spinnerPosition)
        try {
            sensorDataManager.updateSensorConfig(sensorType, intervalSeconds)
            Toast.makeText(
                this,
                "${sensorType.name} interval updated to ${intervalSeconds}s",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error updating interval: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorDataManager.cleanup()
    }

    /**
     * Data class to hold sensor card views
     */
    private data class SensorCardViews(
        val sensorSwitch: SwitchMaterial,
        val intervalSpinner: Spinner,
        val statusText: TextView?,
        val lastValueText: TextView?,
        val lastTimestampText: TextView?
    )
}
