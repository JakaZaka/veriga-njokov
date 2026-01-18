package com.example.closy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.closy.events.EventManager
import com.example.closy.model.EventType
import com.example.closy.network.NetworkManager
import com.example.closy.simulation.*
import com.google.android.material.textfield.TextInputEditText

class SimulationActivity : AppCompatActivity() {

    private lateinit var simulationManager: SimulationManager
    private lateinit var eventManager: EventManager
    private lateinit var networkManager: NetworkManager

    private var currentConfig: SimulationConfig? = null
    private var isSimulationRunning = false

    // Activity result launcher for map location picker
    private val mapLocationPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                val latitude = data.getDoubleExtra("latitude", 46.5547)
                val longitude = data.getDoubleExtra("longitude", 15.6459)
                latitudeInput.setText(latitude.toString())
                longitudeInput.setText(longitude.toString())
                updateDisplays()
                Toast.makeText(this, "Lokacija posodobljena", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // UI components
    private lateinit var storeSpinner: Spinner
    private lateinit var rangeDisplay: TextView
    private lateinit var intervalDisplay: TextView
    private lateinit var locationDisplay: TextView
    private lateinit var statusText: TextView

    private lateinit var minValueInput: TextInputEditText
    private lateinit var maxValueInput: TextInputEditText
    private lateinit var intervalInput: TextInputEditText
    private lateinit var latitudeInput: TextInputEditText
    private lateinit var longitudeInput: TextInputEditText

    private lateinit var startStopButton: Button
    private lateinit var selectOnMapButton: Button

    // Store data with locations and MongoDB IDs
    data class Store(
        val name: String,
        val address: String,
        val lat: Double,
        val lng: Double,
        val storeId: String? = null  // MongoDB ObjectID, null for "Drugo"
    )

    private val stores = listOf(
        Store("H&M", "Vetrinjska ulica 22, Maribor", 46.5589617, 15.6479913, "683c82e19ebb2e3b6cd224b3"),
        Store("ZARA", "Pobreška cesta 18, Maribor", 46.5534787, 15.6531013, "6830fc0250fe3e4f4364aef7"),
        Store("Drugo", "Maribor", 46.5576, 15.6456, null)  // No store_id for "Drugo"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simulation)

        // Setup back button
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        // 1. Določimo privzeti URL z uporabo vašega dinamičnega SERVER_IP
        // Gradle zdaj samodejno vstavi vaš Wi-Fi IP naslov tukaj
        val defaultUrl = "http://${BuildConfig.SERVER_IP}:5000/api"

        // 2. Preberemo shranjen URL iz nastavitev, če obstaja, sicer uporabimo privzetega
        val serverUrl = getSharedPreferences("ClosyPreferences", MODE_PRIVATE)
            .getString("server_url", defaultUrl) ?: defaultUrl

        // 3. Inicializiramo managerje s pravilnim URL-jem
        networkManager = NetworkManager(serverUrl)
        eventManager = EventManager(this, networkManager)
        simulationManager = SimulationManager(this, eventManager)

        // 4. Nastavimo preostali del UI
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

        minValueInput = findViewById(R.id.minValueInput)
        maxValueInput = findViewById(R.id.maxValueInput)
        intervalInput = findViewById(R.id.intervalInput)
        latitudeInput = findViewById(R.id.latitudeInput)
        longitudeInput = findViewById(R.id.longitudeInput)

        startStopButton = findViewById(R.id.startStopButton)
        selectOnMapButton = findViewById(R.id.selectOnMapButton)
    }

    private fun setupStoreSpinner() {
        val storeNames = stores.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, storeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        storeSpinner.adapter = adapter

        storeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Set location inputs to selected store's location
                val selectedStore = stores[position]
                latitudeInput.setText(selectedStore.lat.toString())
                longitudeInput.setText(selectedStore.lng.toString())
                updateDisplays()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {

        startStopButton.setOnClickListener {
            if (isSimulationRunning) {
                stopSimulation()
            } else {
                startSimulation()
            }
        }

        selectOnMapButton.setOnClickListener {
            openMapLocationPicker()
        }

        // Update displays when values change
        minValueInput.setOnFocusChangeListener { _, _ -> updateDisplays() }
        maxValueInput.setOnFocusChangeListener { _, _ -> updateDisplays() }
        intervalInput.setOnFocusChangeListener { _, _ -> updateDisplays() }
        latitudeInput.setOnFocusChangeListener { _, _ -> updateDisplays() }
        longitudeInput.setOnFocusChangeListener { _, _ -> updateDisplays() }
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
            val latitude = latitudeInput.text.toString().toDouble()
            val longitude = longitudeInput.text.toString().toDouble()
            val selectedStore = stores[storeSpinner.selectedItemPosition]

            if (minVal >= maxVal) {
                Toast.makeText(this, "Spodnja meja mora biti manjša od zgornje!", Toast.LENGTH_LONG).show()
                return
            }

            if (interval <= 0) {
                Toast.makeText(this, "Interval mora biti večji od 0!", Toast.LENGTH_LONG).show()
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
                    latitude = latitude,
                    longitude = longitude
                ),
                valueType = ValueType.RANDOM,
                descriptionTemplate = DescriptionTemplate.PEOPLE_COUNT,
                enabled = true,
                storeId = selectedStore.storeId  // Include store_id (null for "Drugo")
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
            startSimulation()
        }
    }

    private fun openMapLocationPicker() {
        val currentLat = latitudeInput.text.toString().toDoubleOrNull() ?: 46.5547
        val currentLng = longitudeInput.text.toString().toDoubleOrNull() ?: 15.6459

        val intent = Intent(this, MapLocationPickerActivity::class.java).apply {
            putExtra("latitude", currentLat)
            putExtra("longitude", currentLng)
        }
        mapLocationPickerLauncher.launch(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't stop simulations on destroy - they should continue in background
        // State is saved in saveState()
    }
}

