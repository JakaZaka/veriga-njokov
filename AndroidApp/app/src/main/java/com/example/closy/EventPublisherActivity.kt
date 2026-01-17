package com.example.closy

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.closy.events.EventManager
import com.example.closy.model.EventType
import com.example.closy.model.LocationData
import com.example.closy.network.NetworkManager
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson

/**
 * Store data with location in Maribor
 */
data class Store(
    val name: String,
    val location: LocationData
)

/**
 * Activity for publishing Digital Twin Events
 */
class EventPublisherActivity : AppCompatActivity() {

    private lateinit var eventManager: EventManager
    private lateinit var networkManager: NetworkManager

    // UI components
    private lateinit var storeSpinner: Spinner
    private lateinit var eventTypeSpinner: Spinner
    private lateinit var titleInput: TextInputEditText
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var locationText: TextView
    private lateinit var publishButton: Button
    private lateinit var clearButton: Button
    private lateinit var viewHistoryButton: Button
    private lateinit var eventHistoryText: TextView

    // Store locations in Maribor
    private val stores = listOf(
        Store("H&M", LocationData(46.5576, 15.6456, null, null)),  // Europark Maribor
        Store("Zara", LocationData(46.5578, 15.6458, null, null)),  // Europark Maribor
        Store("Pull&Bear", LocationData(46.5574, 15.6454, null, null)),  // Europark Maribor
        Store("Bershka", LocationData(46.5580, 15.6460, null, null)),  // Europark Maribor
        Store("Mango", LocationData(46.5572, 15.6452, null, null)),  // Europark Maribor
        Store("Reserved", LocationData(46.5582, 15.6462, null, null)),  // Europark Maribor
        Store("C&A", LocationData(46.5570, 15.6450, null, null)),  // Europark Maribor
        Store("New Yorker", LocationData(46.5584, 15.6464, null, null)),  // Europark Maribor
        Store("Drugo", LocationData(46.5576, 15.6456, null, null))  // Default Europark location
    )

    private var selectedStoreLocation: LocationData? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_publisher)

        // Setup back button
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Initialize network and event manager
        val serverUrl = getSharedPreferences("ClosyPreferences", MODE_PRIVATE)
            .getString("server_url", "http://your-server.com/api/sensor-data") ?: ""
        networkManager = NetworkManager(serverUrl)
        eventManager = EventManager(this, networkManager)

        // Initialize UI
        initializeViews()
        setupStoreSpinner()
        setupEventCategorySpinner()
        setupListeners()
        updateLocationDisplay()

        // Subscribe to events
        eventManager.subscribeToAllEvents("publisher_activity") { event ->
            runOnUiThread {
                updateEventHistory()
            }
        }
    }

    private fun initializeViews() {
        storeSpinner = findViewById(R.id.storeSpinner)
        eventTypeSpinner = findViewById(R.id.eventTypeSpinner)
        titleInput = findViewById(R.id.titleInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        locationText = findViewById(R.id.locationText)
        publishButton = findViewById(R.id.publishButton)
        clearButton = findViewById(R.id.clearButton)
        viewHistoryButton = findViewById(R.id.viewHistoryButton)
        eventHistoryText = findViewById(R.id.eventHistoryText)
    }

    private fun setupStoreSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            stores.map { it.name }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        storeSpinner.adapter = adapter

        // Set listener to update location when store is selected
        storeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedStoreLocation = stores[position].location
                updateLocationDisplay()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedStoreLocation = null
            }
        }

        // Set initial location
        if (stores.isNotEmpty()) {
            selectedStoreLocation = stores[0].location
        }
    }

    private fun setupEventCategorySpinner() {
        val categories = listOf(
            "Zaprtje trgovine",
            "Velika gneča",
            "Prazne police / Razprodano",
            "Tehnična težava",
            "Varnostni incident",
            "Izredna razprodaja",
            "VIP dogodek",
            "Drugo"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        eventTypeSpinner.adapter = adapter
    }

    private fun setupListeners() {
        publishButton.setOnClickListener {
            publishEvent()
        }

        clearButton.setOnClickListener {
            clearForm()
        }

        viewHistoryButton.setOnClickListener {
            showEventHistory()
        }
    }


    private fun publishEvent() {
        val selectedStore = storeSpinner.selectedItem.toString()
        val selectedCategory = eventTypeSpinner.selectedItem.toString()

        val title = titleInput.text.toString()
        val description = descriptionInput.text.toString()

        // Validate input
        if (title.isEmpty()) {
            Toast.makeText(this, "Sporočilo je obvezno", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare metadata
        val metadata = mutableMapOf<String, Any>(
            "store" to selectedStore,
            "category" to selectedCategory
        )

        // Publish event
        publishButton.isEnabled = false

        // Use CUSTOM_EVENT type for extreme events with store location
        eventManager.publishEvent(
            eventType = EventType.CUSTOM_EVENT,
            title = title,
            description = description.ifEmpty { title },
            metadata = metadata,
            customTopic = "store/extreme/event",
            customLocation = selectedStoreLocation
        ) { success, message ->
            runOnUiThread {
                publishButton.isEnabled = true
                if (success) {
                    Toast.makeText(this, "Dogodek uspešno poslan!", Toast.LENGTH_SHORT).show()
                    clearForm()
                } else {
                    Toast.makeText(this, "Napaka: $message", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun clearForm() {
        titleInput.setText("")
        descriptionInput.setText("")
    }

    private fun updateLocationDisplay() {
        selectedStoreLocation?.let { location ->
            locationText.text = "Lat: ${String.format("%.4f", location.latitude)}, " +
                    "Lng: ${String.format("%.4f", location.longitude)}"
        } ?: run {
            locationText.text = "Izberite trgovino"
        }
    }

    private fun updateEventHistory() {
        val recentEvents = eventManager.getEventHistory(limit = 3)
        if (recentEvents.isEmpty()) {
            eventHistoryText.text = "Še ni objavljenih dogodkov"
        } else {
            val historyText = recentEvents.joinToString("\n\n") { event ->
                "${event.title} (${event.topic})\n${event.description}"
            }
            eventHistoryText.text = historyText
        }
    }

    private fun showEventHistory() {
        val events = eventManager.getEventHistory(limit = 50)
        val gson = Gson()
        val json = gson.toJson(events)

        // Show in dialog or new activity
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Zgodovina dogodkov (${events.size})")
            .setMessage(json)
            .setPositiveButton("Zapri", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        eventManager.unsubscribeFromAllEvents("publisher_activity")
        eventManager.cleanup()
    }
}

