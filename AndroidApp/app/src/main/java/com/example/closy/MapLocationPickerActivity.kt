package com.example.closy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/**
 * Activity for picking location on OpenStreetMap
 */
class MapLocationPickerActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var selectedLocation: GeoPoint? = null
    private var marker: Marker? = null
    private lateinit var coordinatesText: TextView
    private lateinit var confirmButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize osmdroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        setContentView(R.layout.activity_map_location_picker)

        coordinatesText = findViewById(R.id.coordinatesText)
        confirmButton = findViewById(R.id.confirmButton)
        mapView = findViewById(R.id.map)

        // Get initial location from intent
        val initialLat = intent.getDoubleExtra("latitude", 46.5547)
        val initialLng = intent.getDoubleExtra("longitude", 15.6459)
        selectedLocation = GeoPoint(initialLat, initialLng)

        // Setup map
        setupMap()

        // Setup buttons
        findViewById<Button>(R.id.cancelButton).setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        confirmButton.setOnClickListener {
            selectedLocation?.let { location ->
                val resultIntent = Intent().apply {
                    putExtra("latitude", location.latitude)
                    putExtra("longitude", location.longitude)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } ?: run {
                Toast.makeText(this, "Prosim izberite lokacijo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupMap() {
        // Configure map
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)

        // Set initial location
        selectedLocation?.let { location ->
            mapView.controller.setCenter(location)
            addMarker(location)
            updateCoordinatesText(location)
        }

        // Add map click listener
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(geoPoint: GeoPoint): Boolean {
                selectedLocation = geoPoint
                addMarker(geoPoint)
                updateCoordinatesText(geoPoint)
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }

        val eventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(eventsOverlay)
    }

    private fun addMarker(location: GeoPoint) {
        // Remove old marker
        marker?.let { mapView.overlays.remove(it) }

        // Add new marker
        marker = Marker(mapView).apply {
            position = location
            title = "Izbrana lokacija"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(this@MapLocationPickerActivity, android.R.drawable.ic_menu_mylocation)
        }
        mapView.overlays.add(marker)
        mapView.invalidate()
    }

    private fun updateCoordinatesText(location: GeoPoint) {
        coordinatesText.text = "Lat: ${String.format("%.6f", location.latitude)}, " +
                "Lng: ${String.format("%.6f", location.longitude)}"
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}

