package com.example.closy.model

import com.google.gson.annotations.SerializedName

/**
 * Event data model for Digital Twin events
 */
data class DigitalTwinEvent(
    @SerializedName("event_id")
    val eventId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("topic")
    val topic: String,  // MQTT-style topic (e.g., "store/clothing/added")

    @SerializedName("location")
    val location: LocationData?,

    @SerializedName("event_type")
    val eventType: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("metadata")
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Predefined event types for Digital Twin
 */
enum class EventType(val topic: String, val displayName: String) {
    // Store events
    CLOTHING_ADDED("store/clothing/added", "Oblačilo dodano"),
    CLOTHING_REMOVED("store/clothing/removed", "Oblačilo odstranjeno"),
    CLOTHING_SOLD("store/clothing/sold", "Oblačilo prodano"),
    INVENTORY_UPDATE("store/inventory/update", "Posodobitev zaloge"),

    // Warehouse events
    WAREHOUSE_ARRIVAL("warehouse/arrival", "Prispetje na skladišče"),
    WAREHOUSE_DISPATCH("warehouse/dispatch", "Odpošiljanje iz skladišča"),
    WAREHOUSE_STOCK_CHECK("warehouse/stock/check", "Preverjanje zaloge"),

    // Weather events
    WEATHER_TEMPERATURE("weather/temperature", "Temperatura"),
    WEATHER_HUMIDITY("weather/humidity", "Vlažnost"),
    WEATHER_CONDITION("weather/condition", "Vremenske razmere"),

    // Device events
    DEVICE_STATUS("device/status", "Status naprave"),
    DEVICE_ALERT("device/alert", "Opozorilo naprave"),
    DEVICE_MAINTENANCE("device/maintenance", "Vzdrževanje naprave"),

    // User events
    USER_ENTERED("user/entered", "Uporabnik vstopil"),
    USER_EXITED("user/exited", "Uporabnik izstopil"),
    USER_ACTION("user/action", "Uporabniška akcija"),

    // Sensor events
    SENSOR_READING("sensor/reading", "Odčitek senzorja"),
    SENSOR_ANOMALY("sensor/anomaly", "Anomalija senzorja"),

    // Custom events
    CUSTOM_EVENT("custom/event", "Poljuben dogodek")
}

/**
 * Event template for quick creation
 */
data class EventTemplate(
    val eventType: EventType,
    val title: String,
    val descriptionTemplate: String,
    val defaultMetadata: Map<String, Any> = emptyMap()
)

/**
 * Predefined event templates
 */
object EventTemplates {
    val templates = listOf(
        EventTemplate(
            eventType = EventType.CLOTHING_ADDED,
            title = "Novo oblačilo dodano",
            descriptionTemplate = "Dodano oblačilo tipa {type} v velikosti {size}",
            defaultMetadata = mapOf("type" to "majica", "size" to "M", "quantity" to 1)
        ),
        EventTemplate(
            eventType = EventType.CLOTHING_SOLD,
            title = "Oblačilo prodano",
            descriptionTemplate = "Prodano oblačilo {item} za {price} EUR",
            defaultMetadata = mapOf("item" to "majica", "price" to 29.99, "quantity" to 1)
        ),
        EventTemplate(
            eventType = EventType.WAREHOUSE_ARRIVAL,
            title = "Dostava na skladišče",
            descriptionTemplate = "Prispela dostava: {items} artiklov",
            defaultMetadata = mapOf("items" to 50, "supplier" to "Dobavitelj X")
        ),
        EventTemplate(
            eventType = EventType.WEATHER_TEMPERATURE,
            title = "Meritev temperature",
            descriptionTemplate = "Temperatura: {temp}°C",
            defaultMetadata = mapOf("temp" to 22.5, "unit" to "celsius")
        ),
        EventTemplate(
            eventType = EventType.USER_ENTERED,
            title = "Vstop stranke",
            descriptionTemplate = "Stranka vstopila v trgovino",
            defaultMetadata = mapOf("count" to 1)
        ),
        EventTemplate(
            eventType = EventType.DEVICE_ALERT,
            title = "Opozorilo naprave",
            descriptionTemplate = "Naprava {device} poroča o {issue}",
            defaultMetadata = mapOf("device" to "Sensor 1", "issue" to "nizka baterija", "severity" to "warning")
        ),
        EventTemplate(
            eventType = EventType.CUSTOM_EVENT,
            title = "Poljuben dogodek",
            descriptionTemplate = "Opis dogodka...",
            defaultMetadata = emptyMap()
        )
    )

    fun getTemplate(eventType: EventType): EventTemplate? {
        return templates.find { it.eventType == eventType }
    }
}

