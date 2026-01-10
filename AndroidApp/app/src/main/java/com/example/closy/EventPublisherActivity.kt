package com.example.closy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.closy.events.EventManager
import com.example.closy.model.EventTemplates
import com.example.closy.model.EventType
import com.example.closy.network.NetworkManager
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson

/**
 * Activity for publishing Digital Twin Events
 */
class EventPublisherActivity : AppCompatActivity() {

    private lateinit var eventManager: EventManager
    private lateinit var networkManager: NetworkManager

    // UI components
    private lateinit var eventTypeSpinner: Spinner
    private lateinit var topicInput: TextInputEditText
    private lateinit var titleInput: TextInputEditText
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var metadataContainer: LinearLayout
    private lateinit var addMetadataButton: Button
    private lateinit var locationText: TextView
    private lateinit var publishButton: Button
    private lateinit var clearButton: Button
    private lateinit var viewHistoryButton: Button
    private lateinit var eventHistoryText: TextView

    // Metadata fields (key-value pairs)
    private val metadataFields = mutableListOf<Pair<EditText, EditText>>()

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
        setupEventTypeSpinner()
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
        eventTypeSpinner = findViewById(R.id.eventTypeSpinner)
        topicInput = findViewById(R.id.topicInput)
        titleInput = findViewById(R.id.titleInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        metadataContainer = findViewById(R.id.metadataContainer)
        addMetadataButton = findViewById(R.id.addMetadataButton)
        locationText = findViewById(R.id.locationText)
        publishButton = findViewById(R.id.publishButton)
        clearButton = findViewById(R.id.clearButton)
        viewHistoryButton = findViewById(R.id.viewHistoryButton)
        eventHistoryText = findViewById(R.id.eventHistoryText)
    }

    private fun setupEventTypeSpinner() {
        val eventTypes = EventType.values()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            eventTypes.map { it.displayName }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        eventTypeSpinner.adapter = adapter

        // Load template when event type changes
        eventTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadEventTemplate(eventTypes[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadEventTemplate(eventType: EventType) {
        val template = EventTemplates.getTemplate(eventType)
        template?.let {
            topicInput.setText(it.eventType.topic)
            titleInput.setText(it.title)
            descriptionInput.setText(it.descriptionTemplate)

            // Clear and load default metadata
            metadataContainer.removeAllViews()
            metadataFields.clear()

            it.defaultMetadata.forEach { (key, value) ->
                addMetadataField(key, value.toString())
            }
        }
    }

    private fun setupListeners() {
        addMetadataButton.setOnClickListener {
            addMetadataField("", "")
        }

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

    private fun addMetadataField(key: String = "", value: String = "") {
        val fieldLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val keyInput = EditText(this).apply {
            hint = "Ključ"
            setText(key)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val valueInput = EditText(this).apply {
            hint = "Vrednost"
            setText(value)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val removeButton = Button(this).apply {
            text = "✕"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                metadataContainer.removeView(fieldLayout)
                metadataFields.remove(Pair(keyInput, valueInput))
            }
        }

        fieldLayout.addView(keyInput)
        fieldLayout.addView(valueInput)
        fieldLayout.addView(removeButton)

        metadataContainer.addView(fieldLayout)
        metadataFields.add(Pair(keyInput, valueInput))
    }

    private fun publishEvent() {
        val selectedPosition = eventTypeSpinner.selectedItemPosition
        val eventType = EventType.values()[selectedPosition]

        val topic = topicInput.text.toString()
        val title = titleInput.text.toString()
        val description = descriptionInput.text.toString()

        // Validate input
        if (title.isEmpty()) {
            Toast.makeText(this, "Naslov je obvezen", Toast.LENGTH_SHORT).show()
            return
        }

        // Collect metadata
        val metadata = mutableMapOf<String, Any>()
        metadataFields.forEach { (keyInput, valueInput) ->
            val key = keyInput.text.toString()
            val value = valueInput.text.toString()
            if (key.isNotEmpty()) {
                // Try to parse as number, otherwise keep as string
                metadata[key] = value.toDoubleOrNull() ?: value
            }
        }

        // Publish event
        publishButton.isEnabled = false
        eventManager.publishEvent(
            eventType = eventType,
            title = title,
            description = description,
            metadata = metadata,
            customTopic = if (topic.isNotEmpty()) topic else null
        ) { success, message ->
            runOnUiThread {
                publishButton.isEnabled = true
                if (success) {
                    Toast.makeText(this, "Dogodek uspešno objavljen!", Toast.LENGTH_SHORT).show()
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
        metadataContainer.removeAllViews()
        metadataFields.clear()

        // Reload template
        val selectedPosition = eventTypeSpinner.selectedItemPosition
        loadEventTemplate(EventType.values()[selectedPosition])
    }

    private fun updateLocationDisplay() {
        // Update location every 5 seconds
        Thread {
            while (!isFinishing) {
                try {
                    Thread.sleep(5000)
                    runOnUiThread {
                        val location = eventManager.getEventHistory(limit = 1)
                            .firstOrNull()?.location

                        if (location != null) {
                            locationText.text = "Lat: ${String.format("%.4f", location.latitude)}, " +
                                    "Lng: ${String.format("%.4f", location.longitude)}"
                        } else {
                            locationText.text = "Lokacija ni na voljo"
                        }
                    }
                } catch (e: InterruptedException) {
                    break
                }
            }
        }.start()
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

