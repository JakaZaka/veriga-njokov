package com.example.closy

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale

/**
 * Main Activity - Control Panel / Overview
 *
 * This is the landing screen that shows:
 * - Server configuration
 * - Global system status (server connection, active sensors, active simulations)
 * - Navigation to main features:
 *   - Sensors Management
 *   - Simulated Sensors
 *   - Extreme Events
 *   - History
 */
class MainActivity : AppCompatActivity() {

    private lateinit var serverUrlInput: TextInputEditText
    private lateinit var serverStatusText: TextView
    private lateinit var activeSensorsText: TextView
    private lateinit var activeSimulationsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved language before setting content view
        applySavedLanguage()

        // Apply saved dark mode preference
        applySavedDarkMode()

        setContentView(R.layout.activity_main)

        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

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

        // Add Clothing Card - Nova pot do vašega modula za oblačila
        findViewById<MaterialCardView>(R.id.addClothingCard)?.setOnClickListener {
            val intent = Intent(this, AddClothingActivity::class.java)
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

        val defaultUrl = "http://${BuildConfig.SERVER_IP}:5000/api"

        val savedUrl = prefs.getString("server_url", defaultUrl)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun applySavedLanguage() {
        val prefs = getSharedPreferences("ClosyPreferences", Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString("app_language", "en") ?: "en"

        val locale = Locale(savedLanguage)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun applySavedDarkMode() {
        val prefs = getSharedPreferences("ClosyPreferences", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
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

