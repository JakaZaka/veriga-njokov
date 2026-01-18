package com.example.closy.events

import android.content.Context
import com.example.closy.model.DigitalTwinEvent
import com.example.closy.model.EventType
import com.example.closy.model.LocationData
import com.example.closy.network.NetworkManager
import com.example.closy.sensors.LocationProvider
import kotlinx.coroutines.*
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Event Manager for Digital Twin Events
 * Handles creation, publishing, and management of events
 */
class EventManager(
    private val context: Context,
    private val networkManager: NetworkManager
) {
    private val locationProvider = LocationProvider(context)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Event history (in-memory storage)
    private val eventHistory = mutableListOf<DigitalTwinEvent>()
    private val maxHistorySize = 1000

    // Event listeners
    private val eventListeners = ConcurrentHashMap<String, (DigitalTwinEvent) -> Unit>()

    // Topic subscribers
    private val topicSubscribers = ConcurrentHashMap<String, MutableList<(DigitalTwinEvent) -> Unit>>()

    init {
        locationProvider.startLocationUpdates()
    }

    /**
     * Publish a new event
     */
    fun publishEvent(
        eventType: EventType,
        title: String,
        description: String,
        metadata: Map<String, Any> = emptyMap(),
        customTopic: String? = null,
        customLocation: LocationData? = null,
        storeId: String? = null,
        callback: ((Boolean, String?) -> Unit)? = null
    ) {
        scope.launch {
            try {
                val event = createEvent(
                    eventType = eventType,
                    title = title,
                    description = description,
                    metadata = metadata,
                    customTopic = customTopic,
                    customLocation = customLocation,
                    storeId = storeId
                )

                println("EventManager: Created event with store_id: ${event.storeId}")
                println("EventManager: Event location: ${event.location}")

                // Add to history
                addToHistory(event)

                // Notify listeners
                notifyListeners(event)

                // Send to server
                withContext(Dispatchers.IO) {
                    networkManager.sendEventData(event) { success, message ->
                        callback?.invoke(success, message)
                    }
                }
            } catch (e: Exception) {
                println("EventManager: Error publishing event: ${e.message}")
                e.printStackTrace()
                callback?.invoke(false, "Error publishing event: ${e.message}")
            }
        }
    }

    /**
     * Create a Digital Twin Event
     */
    private fun createEvent(
        eventType: EventType,
        title: String,
        description: String,
        metadata: Map<String, Any>,
        customTopic: String?,
        customLocation: LocationData? = null,
        storeId: String? = null
    ): DigitalTwinEvent {
        return DigitalTwinEvent(
            eventId = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            topic = customTopic ?: eventType.topic,
            location = customLocation ?: locationProvider.getCurrentLocation(),
            eventType = eventType.name,
            title = title,
            description = description,
            metadata = metadata,
            storeId = storeId
        )
    }

    /**
     * Publish multiple events in batch
     */
    fun publishBatchEvents(
        events: List<DigitalTwinEvent>,
        callback: ((Boolean, String?) -> Unit)? = null
    ) {
        scope.launch {
            try {
                // Add all to history
                events.forEach { addToHistory(it) }

                // Notify listeners
                events.forEach { notifyListeners(it) }

                // Send to server
                withContext(Dispatchers.IO) {
                    networkManager.sendBatchEvents(events) { success, message ->
                        callback?.invoke(success, message)
                    }
                }
            } catch (e: Exception) {
                callback?.invoke(false, "Error publishing batch: ${e.message}")
            }
        }
    }

    /**
     * Subscribe to specific topic (MQTT-style)
     */
    fun subscribeToTopic(topic: String, listener: (DigitalTwinEvent) -> Unit) {
        val subscribers = topicSubscribers.getOrPut(topic) { mutableListOf() }
        subscribers.add(listener)
    }

    /**
     * Unsubscribe from topic
     */
    fun unsubscribeFromTopic(topic: String, listener: (DigitalTwinEvent) -> Unit) {
        topicSubscribers[topic]?.remove(listener)
    }

    /**
     * Subscribe to all events
     */
    fun subscribeToAllEvents(id: String, listener: (DigitalTwinEvent) -> Unit) {
        eventListeners[id] = listener
    }

    /**
     * Unsubscribe from all events
     */
    fun unsubscribeFromAllEvents(id: String) {
        eventListeners.remove(id)
    }

    /**
     * Get event history
     */
    fun getEventHistory(
        topic: String? = null,
        eventType: EventType? = null,
        limit: Int = 100
    ): List<DigitalTwinEvent> {
        var filtered = eventHistory.toList()

        // Filter by topic (supports wildcards)
        topic?.let { topicFilter ->
            filtered = filtered.filter { event ->
                matchesTopic(event.topic, topicFilter)
            }
        }

        // Filter by event type
        eventType?.let { typeFilter ->
            filtered = filtered.filter { it.eventType == typeFilter.name }
        }

        return filtered.takeLast(limit)
    }

    /**
     * Get events count by topic
     */
    fun getEventCountByTopic(): Map<String, Int> {
        return eventHistory.groupingBy { it.topic }.eachCount()
    }

    /**
     * Clear event history
     */
    fun clearHistory() {
        eventHistory.clear()
    }

    /**
     * Export events as JSON
     */
    fun exportEventsAsJson(): String {
        return com.google.gson.Gson().toJson(eventHistory)
    }

    /**
     * Add event to history
     */
    private fun addToHistory(event: DigitalTwinEvent) {
        synchronized(eventHistory) {
            eventHistory.add(event)

            // Keep only last maxHistorySize events
            if (eventHistory.size > maxHistorySize) {
                eventHistory.removeAt(0)
            }
        }
    }

    /**
     * Notify all listeners
     */
    private fun notifyListeners(event: DigitalTwinEvent) {
        // Notify global listeners
        eventListeners.values.forEach { listener ->
            try {
                listener(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Notify topic subscribers
        topicSubscribers.forEach { (topic, listeners) ->
            if (matchesTopic(event.topic, topic)) {
                listeners.forEach { listener ->
                    try {
                        listener(event)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    /**
     * Check if event topic matches subscription topic (supports MQTT wildcards)
     * + matches single level
     * # matches multiple levels
     */
    private fun matchesTopic(eventTopic: String, subscriptionTopic: String): Boolean {
        // Exact match
        if (eventTopic == subscriptionTopic) return true

        // # wildcard matches everything
        if (subscriptionTopic == "#") return true

        val eventParts = eventTopic.split("/")
        val subParts = subscriptionTopic.split("/")

        // Multi-level wildcard (#)
        if (subParts.last() == "#") {
            val baseSubParts = subParts.dropLast(1)
            if (eventParts.size >= baseSubParts.size) {
                return eventParts.take(baseSubParts.size) == baseSubParts
            }
            return false
        }

        // Must have same number of levels if no multi-level wildcard
        if (eventParts.size != subParts.size) return false

        // Check each level (+ is single-level wildcard)
        return eventParts.zip(subParts).all { (eventPart, subPart) ->
            subPart == "+" || eventPart == subPart
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        locationProvider.stopLocationUpdates()
        scope.cancel()
        eventListeners.clear()
        topicSubscribers.clear()
    }
}

