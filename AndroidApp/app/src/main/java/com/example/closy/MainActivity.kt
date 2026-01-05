package com.example.closy

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText

/**
 * Main Activity - Control Panel / Overview
 *
 * This is the landing screen that shows:
 * - Server configuration
 * - Global system status (server connection, active sensors, active simulations)
 * - Navigation to main features:
 *   - Sensors Management
 *   - Simulated Sensors
 *   - Manual Events
 *   - History
 */
class MainActivity : AppCompatActivity() {

    private lateinit var serverUrlInput: TextInputEditText
    private lateinit var serverStatusText: TextView
    private lateinit var activeSensorsText: TextView
    private lateinit var activeSimulationsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        serverUrlInput = findViewById(R.id.serverUrlInput)
        serverStatusText = findViewById(R.id.serverStatusText)
        activeSensorsText = findViewById(R.id.activeSensorsText)
        activeSimulationsText = findViewById(R.id.activeSimulationsText)

        // Load saved server URL
        loadServerUrl()

        // Save server URL when changed
        serverUrlInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveServerUrl()
            }
        }

        // Setup navigation cards
        setupNavigation()

        // Update status
        updateGlobalStatus()
    }

    override fun onResume() {
        super.onResume()
        // Update status when returning to this screen
        updateGlobalStatus()
    }

    private fun setupNavigation() {
        // Sensors Card
        findViewById<MaterialCardView>(R.id.sensorsCard).setOnClickListener {
            val intent = Intent(this, SensorsActivity::class.java)
            startActivity(intent)
        }

        // Simulation Card
        findViewById<MaterialCardView>(R.id.simulationCard).setOnClickListener {
            val intent = Intent(this, SimulationActivity::class.java)
            startActivity(intent)
        }

        // Events Card
        findViewById<MaterialCardView>(R.id.eventsCard).setOnClickListener {
            val intent = Intent(this, EventPublisherActivity::class.java)
            startActivity(intent)
        }

        // History Card (Optional - placeholder for now)
        findViewById<MaterialCardView>(R.id.historyCard).setOnClickListener {
            // TODO: Implement history screen
            android.widget.Toast.makeText(
                this,
                "History feature coming soon",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadServerUrl() {
        val prefs = getSharedPreferences("ClosyPreferences", MODE_PRIVATE)
        val savedUrl = prefs.getString("server_url", "http://your-server.com/api/sensor-data")
        serverUrlInput.setText(savedUrl)
    }

    private fun saveServerUrl() {
        val url = serverUrlInput.text.toString()
        val prefs = getSharedPreferences("ClosyPreferences", MODE_PRIVATE)
        prefs.edit().putString("server_url", url).apply()
    }

    private fun updateGlobalStatus() {
        // Update server status (placeholder - would check actual connection)
        val isConnected = checkServerConnection()
        if (isConnected) {
            serverStatusText.text = "● Connected"
            serverStatusText.setTextColor(getColor(android.R.color.holo_green_dark))
        } else {
            serverStatusText.text = "● Disconnected"
            serverStatusText.setTextColor(getColor(android.R.color.holo_red_dark))
        }

        // Update active sensors count (placeholder)
        val activeSensors = getActiveSensorsCount()
        activeSensorsText.text = "$activeSensors / 7"

        // Update active simulations count (placeholder)
        val activeSimulations = getActiveSimulationsCount()
        activeSimulationsText.text = "$activeSimulations"
    }

    private fun checkServerConnection(): Boolean {
        // TODO: Implement actual server connection check
        // For now, just check if URL is configured
        val url = serverUrlInput.text.toString()
        return url.isNotEmpty() && url.startsWith("http")
    }

    private fun getActiveSensorsCount(): Int {
        // TODO: Query SensorDataManager for active sensors
        // For now return 0
        return 0
    }

    private fun getActiveSimulationsCount(): Int {
        // TODO: Query SimulationManager for active simulations
        // For now return 0
        return 0
    }
}

