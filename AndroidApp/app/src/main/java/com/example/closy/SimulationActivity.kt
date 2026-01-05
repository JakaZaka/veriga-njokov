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

    // UI components
    private lateinit var simulationNameLabel: TextView
    private lateinit var rangeDisplay: TextView
    private lateinit var intervalDisplay: TextView
    private lateinit var locationDisplay: TextView
    private lateinit var statusText: TextView
    private lateinit var simulationToggle: SwitchMaterial

    private lateinit var minValueInput: TextInputEditText
    private lateinit var maxValueInput: TextInputEditText
    private lateinit var intervalInput: TextInputEditText
    private lateinit var unitInput: TextInputEditText
    private lateinit var locationInput: TextInputEditText

    private lateinit var enableExtremeCheck: CheckBox
    private lateinit var extremeThresholdInput: TextInputEditText
    private lateinit var extremeConditionSpinner: Spinner

    private lateinit var activeSimulationsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simulation)

        // Setup back button
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Initialize managers
        val serverUrl = getSharedPreferences("ClosyPreferences", MODE_PRIVATE)
            .getString("server_url", "http://your-server.com/api/sensor-data") ?: ""
        networkManager = NetworkManager(serverUrl)
        eventManager = EventManager(this, networkManager)
        simulationManager = SimulationManager(this, eventManager)

        initializeViews()
        setupTemplateButtons()
        setupExtremeConditionSpinner()
        setupListeners()
        updateActiveSimulationsList()
    }

    private fun initializeViews() {
        simulationNameLabel = findViewById(R.id.simulationNameLabel)
        rangeDisplay = findViewById(R.id.rangeDisplay)
        intervalDisplay = findViewById(R.id.intervalDisplay)
        locationDisplay = findViewById(R.id.locationDisplay)
        statusText = findViewById(R.id.statusText)
        simulationToggle = findViewById(R.id.simulationToggle)

        minValueInput = findViewById(R.id.minValueInput)
        maxValueInput = findViewById(R.id.maxValueInput)
        intervalInput = findViewById(R.id.intervalInput)
        unitInput = findViewById(R.id.unitInput)
        locationInput = findViewById(R.id.locationInput)

        enableExtremeCheck = findViewById(R.id.enableExtremeCheck)
        extremeThresholdInput = findViewById(R.id.extremeThresholdInput)
        extremeConditionSpinner = findViewById(R.id.extremeConditionSpinner)

        activeSimulationsText = findViewById(R.id.activeSimulationsText)
    }

    private fun setupTemplateButtons() {
        findViewById<Button>(R.id.templateStoreCapacity).setOnClickListener {
            loadTemplate(SimulationTemplates.getStoreCapacityTemplate())
        }

        findViewById<Button>(R.id.templateTemperature).setOnClickListener {
            loadTemplate(SimulationTemplates.getTemperatureTemplate())
        }

        findViewById<Button>(R.id.templateHumidity).setOnClickListener {
            loadTemplate(SimulationTemplates.getHumidityTemplate())
        }

        findViewById<Button>(R.id.templateStockLevel).setOnClickListener {
            loadTemplate(SimulationTemplates.getStockLevelTemplate())
        }
    }

    private fun setupExtremeConditionSpinner() {
        val conditions = arrayOf("Večje od", "Manjše od", "Enako")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, conditions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        extremeConditionSpinner.adapter = adapter
    }

    private fun setupListeners() {
        simulationToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startSimulation()
            } else {
                stopCurrentSimulation()
            }
            updateStatusDisplay(isChecked)
        }

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            saveConfiguration()
        }

        findViewById<Button>(R.id.stopAllButton).setOnClickListener {
            stopAllSimulations()
        }

        // Update displays when values change
        minValueInput.setOnFocusChangeListener { _, _ -> updateDisplays() }
        maxValueInput.setOnFocusChangeListener { _, _ -> updateDisplays() }
        intervalInput.setOnFocusChangeListener { _, _ -> updateDisplays() }
        locationInput.setOnFocusChangeListener { _, _ -> updateDisplays() }
    }

    private fun loadTemplate(template: SimulationConfig) {
        currentConfig = template

        // Update name
        simulationNameLabel.text = template.name

        // Update inputs
        minValueInput.setText(template.minValue.toString())
        maxValueInput.setText(template.maxValue.toString())
        intervalInput.setText(template.intervalMinutes.toString())
        unitInput.setText(template.unit)

        // Update location
        val locationText = template.manualLocation?.address ?: "Trenutna lokacija"
        locationInput.setText(locationText)

        // Update extreme settings
        enableExtremeCheck.isChecked = template.extremeThreshold != null
        extremeThresholdInput.setText(template.extremeThreshold?.toString() ?: "")

        when (template.extremeCondition) {
            ExtremeCondition.GREATER_THAN -> extremeConditionSpinner.setSelection(0)
            ExtremeCondition.LESS_THAN -> extremeConditionSpinner.setSelection(1)
            ExtremeCondition.EQUALS -> extremeConditionSpinner.setSelection(2)
            null -> extremeConditionSpinner.setSelection(0)
        }

        updateDisplays()
        Toast.makeText(this, "Naložena predloga: ${template.name}", Toast.LENGTH_SHORT).show()
    }

    private fun updateDisplays() {
        try {
            val minVal = minValueInput.text.toString().toDoubleOrNull() ?: 0.0
            val maxVal = maxValueInput.text.toString().toDoubleOrNull() ?: 100.0
            val interval = intervalInput.text.toString().toIntOrNull() ?: 10
            val unit = unitInput.text.toString()
            val location = locationInput.text.toString()

            rangeDisplay.text = "From ${String.format("%.2f", minVal)} to ${String.format("%.2f", maxVal)}"
            intervalDisplay.text = "Every $interval minutes"
            locationDisplay.text = location
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveConfiguration() {
        try {
            val minVal = minValueInput.text.toString().toDouble()
            val maxVal = maxValueInput.text.toString().toDouble()
            val interval = intervalInput.text.toString().toInt()
            val unit = unitInput.text.toString()
            val location = locationInput.text.toString()

            val extremeThreshold = if (enableExtremeCheck.isChecked) {
                extremeThresholdInput.text.toString().toDoubleOrNull()
            } else null

            val extremeCondition = if (enableExtremeCheck.isChecked) {
                when (extremeConditionSpinner.selectedItemPosition) {
                    0 -> ExtremeCondition.GREATER_THAN
                    1 -> ExtremeCondition.LESS_THAN
                    2 -> ExtremeCondition.EQUALS
                    else -> ExtremeCondition.GREATER_THAN
                }
            } else null

            val template = currentConfig ?: SimulationTemplates.getStoreCapacityTemplate()

            currentConfig = template.copy(
                minValue = minVal,
                maxValue = maxVal,
                intervalMinutes = interval,
                unit = unit,
                manualLocation = ManualLocation(
                    address = location,
                    latitude = template.manualLocation?.latitude ?: 46.5547,
                    longitude = template.manualLocation?.longitude ?: 15.6466
                ),
                extremeThreshold = extremeThreshold,
                extremeCondition = extremeCondition
            )

            updateDisplays()
            Toast.makeText(this, "Nastavitve shranjene", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Napaka pri shranjevanju: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun startSimulation() {
        val config = currentConfig
        if (config == null) {
            Toast.makeText(this, "Najprej izberite predlogo", Toast.LENGTH_SHORT).show()
            simulationToggle.isChecked = false
            return
        }

        simulationManager.startSimulation(config)
        Toast.makeText(this, "Simulacija zagnana: ${config.name}", Toast.LENGTH_SHORT).show()
        updateActiveSimulationsList()
    }

    private fun stopCurrentSimulation() {
        currentConfig?.let {
            simulationManager.stopSimulation(it.id)
            Toast.makeText(this, "Simulacija ustavljena", Toast.LENGTH_SHORT).show()
            updateActiveSimulationsList()
        }
    }

    private fun stopAllSimulations() {
        simulationManager.stopAllSimulations()
        simulationToggle.isChecked = false
        Toast.makeText(this, "Vse simulacije ustavljene", Toast.LENGTH_SHORT).show()
        updateActiveSimulationsList()
    }

    private fun updateStatusDisplay(isEnabled: Boolean) {
        if (isEnabled) {
            statusText.text = "Enabled"
            statusText.setTextColor(getColor(android.R.color.holo_green_light))
        } else {
            statusText.text = "Disabled"
            statusText.setTextColor(getColor(android.R.color.holo_red_light))
        }
    }

    private fun updateActiveSimulationsList() {
        val activeSimulations = simulationManager.getActiveSimulations()
        if (activeSimulations.isEmpty()) {
            activeSimulationsText.text = "Ni aktivnih simulacij"
        } else {
            activeSimulationsText.text = "Aktivne simulacije (${activeSimulations.size}):\n" +
                    activeSimulations.joinToString("\n") { "• Simulacija" }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't stop simulations on destroy - they should continue in background
    }
}

