package com.example.closy

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.closy.events.EventManager
import com.example.closy.model.EventType
import com.example.closy.network.NetworkManager
import com.example.closy.simulation.*
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText

class SimulationActivity : AppCompatActivity() {

    private lateinit var simulationManager: SimulationManager
    private lateinit var eventManager: EventManager
    private lateinit var networkManager: NetworkManager

    private var currentConfig: SimulationConfig? = null
    private var isSimulationRunning = false

    // UI components
    private lateinit var storeSpinner: Spinner
    private lateinit var rangeDisplay: TextView
    private lateinit var intervalDisplay: TextView
    private lateinit var locationDisplay: TextView
    private lateinit var statusText: TextView
    private lateinit var simulationToggle: SwitchMaterial

    private lateinit var minValueInput: TextInputEditText
    private lateinit var maxValueInput: TextInputEditText
    private lateinit var intervalInput: TextInputEditText

    private lateinit var startStopButton: Button

    // Store data with locations
    data class Store(val name: String, val address: String, val lat: Double, val lng: Double)

    private val stores = listOf(
        Store("H&M", "Gosposvetska cesta 5, Maribor", 46.5547, 15.6459),
        Store("Zara", "Gosposvetska cesta 5, Maribor", 46.5547, 15.6461),
        Store("C&A", "Gosposvetska cesta 5, Maribor", 46.5547, 15.6463),
        Store("New Yorker", "Gosposvetska cesta 5, Maribor", 46.5547, 15.6465),
        Store("Deichmann", "Gosposvetska cesta 5, Maribor", 46.5547, 15.6467),
        Store("dm", "Gosposvetska cesta 5, Maribor", 46.5547, 15.6469)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simulation)

        // Setup back button
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Initialize managers
        val serverUrl = getSharedPreferences("ClosyPreferences", MODE_PRIVATE)
            .getString("server_url", "http://10.0.2.2:5000/api") ?: ""
        networkManager = NetworkManager(serverUrl)
        eventManager = EventManager(this, networkManager)
        simulationManager = SimulationManager(this, eventManager)

        initializeViews()
        setupStoreSpinner()
        setupListeners()
        loadSavedState()
    }

    private fun initializeViews() {
        storeSpinner = findViewById(R.id.storeSpinner)
        rangeDisplay = findViewById(R.id.rangeDisplay)
        intervalDisplay = findViewById(R.id.intervalDisplay)
        locationDisplay = findViewById(R.id.locationDisplay)
        statusText = findViewById(R.id.statusText)
        simulationToggle = findViewById(R.id.simulationToggle)

        minValueInput = findViewById(R.id.minValueInput)
        maxValueInput = findViewById(R.id.maxValueInput)
        intervalInput = findViewById(R.id.intervalInput)

        startStopButton = findViewById(R.id.startStopButton)
    }

    private fun setupStoreSpinner() {
        val storeNames = stores.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, storeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        storeSpinner.adapter = adapter

        storeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateDisplays()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {
        simulationToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isSimulationRunning) {
                startSimulation()
            } else if (!isChecked && isSimulationRunning) {
                stopSimulation()
            }
        }

        startStopButton.setOnClickListener {
            if (isSimulationRunning) {
                stopSimulation()
            } else {
                startSimulation()
            }
        }

        // Update displays when values change
        minValueInput.setOnFocusChangeListener { _, _ -> updateDisplays() }
        maxValueInput.setOnFocusChangeListener { _, _ -> updateDisplays() }
        intervalInput.setOnFocusChangeListener { _, _ -> updateDisplays() }
    }

    private fun updateDisplays() {
        try {
            val minVal = minValueInput.text.toString().toIntOrNull() ?: 0
            val maxVal = maxValueInput.text.toString().toIntOrNull() ?: 100
            val interval = intervalInput.text.toString().toIntOrNull() ?: 5
            val selectedStore = stores[storeSpinner.selectedItemPosition]

            rangeDisplay.text = "📊 Od $minVal do $maxVal ljudi"
            intervalDisplay.text = "⏱️ Vsakih $interval minut"
            locationDisplay.text = "📍 ${selectedStore.name}, ${selectedStore.address}"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startSimulation() {
        try {
            val minVal = minValueInput.text.toString().toDouble()
            val maxVal = maxValueInput.text.toString().toDouble()
            val interval = intervalInput.text.toString().toInt()
            val selectedStore = stores[storeSpinner.selectedItemPosition]

            if (minVal >= maxVal) {
                Toast.makeText(this, "Spodnja meja mora biti manjša od zgornje!", Toast.LENGTH_LONG).show()
                simulationToggle.isChecked = false
                return
            }

            if (interval <= 0) {
                Toast.makeText(this, "Interval mora biti večji od 0!", Toast.LENGTH_LONG).show()
                simulationToggle.isChecked = false
                return
            }

            currentConfig = SimulationConfig(
                id = "store_simulation",
                name = "Število ljudi v trgovini ${selectedStore.name}",
                topic = "store/${selectedStore.name.lowercase().replace(" ", "_")}/people_count",
                eventType = EventType.USER_ACTION,
                minValue = minVal,
                maxValue = maxVal,
                unit = "ljudi",
                intervalMinutes = interval,
                manualLocation = ManualLocation(
                    address = "${selectedStore.name}, ${selectedStore.address}",
                    latitude = selectedStore.lat,
                    longitude = selectedStore.lng
                ),
                valueType = ValueType.RANDOM,
                descriptionTemplate = DescriptionTemplate.PEOPLE_COUNT,
                enabled = true
            )

            currentConfig?.let {
                simulationManager.startSimulation(it)
                isSimulationRunning = true
                updateStatusDisplay(true)
                startStopButton.text = "Ustavi simulacijo"
                startStopButton.backgroundTintList = getColorStateList(android.R.color.holo_red_light)
                saveState()
                Toast.makeText(this, "✅ Simulacija zagnana za ${selectedStore.name}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Napaka: ${e.message}", Toast.LENGTH_LONG).show()
            simulationToggle.isChecked = false
        }
    }

    private fun stopSimulation() {
        currentConfig?.let {
            simulationManager.stopSimulation(it.id)
            isSimulationRunning = false
            updateStatusDisplay(false)
            startStopButton.text = "Zaženi simulacijo"
            startStopButton.backgroundTintList = getColorStateList(android.R.color.holo_green_light)
            saveState()
            Toast.makeText(this, "⏹️ Simulacija ustavljena", Toast.LENGTH_SHORT).show()
        }
        simulationToggle.isChecked = false
    }

    private fun updateStatusDisplay(isEnabled: Boolean) {
        if (isEnabled) {
            statusText.text = "Omogočeno"
            statusText.setTextColor(getColor(android.R.color.holo_green_light))
        } else {
            statusText.text = "Onemogočeno"
            statusText.setTextColor(getColor(android.R.color.holo_red_light))
        }
    }

    private fun saveState() {
        val prefs = getSharedPreferences("SimulationPreferences", MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("is_running", isSimulationRunning)
            putInt("store_index", storeSpinner.selectedItemPosition)
            putString("min_value", minValueInput.text.toString())
            putString("max_value", maxValueInput.text.toString())
            putString("interval", intervalInput.text.toString())
            apply()
        }
    }

    private fun loadSavedState() {
        val prefs = getSharedPreferences("SimulationPreferences", MODE_PRIVATE)
        val wasRunning = prefs.getBoolean("is_running", false)
        val storeIndex = prefs.getInt("store_index", 0)
        val minValue = prefs.getString("min_value", "0") ?: "0"
        val maxValue = prefs.getString("max_value", "100") ?: "100"
        val interval = prefs.getString("interval", "5") ?: "5"

        storeSpinner.setSelection(storeIndex)
        minValueInput.setText(minValue)
        maxValueInput.setText(maxValue)
        intervalInput.setText(interval)
        updateDisplays()

        if (wasRunning) {
            // Restart simulation
            simulationToggle.isChecked = true
            startSimulation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't stop simulations on destroy - they should continue in background
        // State is saved in saveState()
    }
}

