package com.example.closy.simulation

import android.content.Context
import com.example.closy.events.EventManager
import com.example.closy.model.EventType
import com.example.closy.model.LocationData
import kotlinx.coroutines.*
import kotlin.random.Random

/**
 * Simulation Manager for automatic event generation
 */
class SimulationManager(
    private val context: Context,
    private val eventManager: EventManager
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val activeSimulations = mutableMapOf<String, Job>()

    /**
     * Start a simulation
     */
    fun startSimulation(config: SimulationConfig) {
        // Stop existing simulation with same ID if any
        stopSimulation(config.id)

        val job = scope.launch {
            while (isActive) {
                // Generate simulated value
                val value = generateValue(config)

                // Check for extreme events
                val isExtreme = checkExtremeEvent(config, value)

                // Create event
                val title = if (isExtreme) {
                    "⚠️ EKSTREMNI DOGODEK: ${config.name}"
                } else {
                    config.name
                }

                val description = buildDescription(config, value, isExtreme)

                val metadata = mutableMapOf<String, Any>(
                    "simulated" to true,
                    "value" to value,
                    "unit" to config.unit,
                    "is_extreme" to isExtreme
                )

                // Add custom metadata
                metadata.putAll(config.customMetadata)

                // Publish event
                eventManager.publishEvent(
                    eventType = config.eventType,
                    title = title,
                    description = description,
                    metadata = metadata,
                    customTopic = config.topic
                ) { success, message ->
                    if (!success) {
                        println("Simulation publish failed: $message")
                    }
                }

                // Wait for next interval
                delay(config.intervalMinutes * 60 * 1000L)
            }
        }

        activeSimulations[config.id] = job
    }

    /**
     * Stop a simulation
     */
    fun stopSimulation(id: String) {
        activeSimulations[id]?.cancel()
        activeSimulations.remove(id)
    }

    /**
     * Stop all simulations
     */
    fun stopAllSimulations() {
        activeSimulations.values.forEach { it.cancel() }
        activeSimulations.clear()
    }

    /**
     * Get active simulations
     */
    fun getActiveSimulations(): List<String> {
        return activeSimulations.keys.toList()
    }

    /**
     * Generate simulated value based on config
     */
    private fun generateValue(config: SimulationConfig): Double {
        return when (config.valueType) {
            ValueType.RANDOM -> {
                Random.nextDouble(config.minValue, config.maxValue)
            }
            ValueType.SINE_WAVE -> {
                val time = System.currentTimeMillis() / 1000.0
                val amplitude = (config.maxValue - config.minValue) / 2.0
                val offset = (config.maxValue + config.minValue) / 2.0
                val frequency = 1.0 / (config.intervalMinutes * 60.0)
                amplitude * kotlin.math.sin(2 * Math.PI * frequency * time) + offset
            }
            ValueType.LINEAR_INCREASE -> {
                val range = config.maxValue - config.minValue
                val step = range / 100.0
                (config.minValue + step * (System.currentTimeMillis() % 100)).coerceIn(config.minValue, config.maxValue)
            }
            ValueType.LINEAR_DECREASE -> {
                val range = config.maxValue - config.minValue
                val step = range / 100.0
                (config.maxValue - step * (System.currentTimeMillis() % 100)).coerceIn(config.minValue, config.maxValue)
            }
        }
    }

    /**
     * Check if value represents an extreme event
     */
    private fun checkExtremeEvent(config: SimulationConfig, value: Double): Boolean {
        config.extremeThreshold?.let { threshold ->
            return when (config.extremeCondition) {
                ExtremeCondition.GREATER_THAN -> value > threshold
                ExtremeCondition.LESS_THAN -> value < threshold
                ExtremeCondition.EQUALS -> kotlin.math.abs(value - threshold) < 0.01
                null -> false
            }
        }
        return false
    }

    /**
     * Build description text
     */
    private fun buildDescription(config: SimulationConfig, value: Double, isExtreme: Boolean): String {
        val valueStr = String.format("%.2f", value)
        val extremePrefix = if (isExtreme) "⚠️ OPOZORILO: " else ""

        return when (config.descriptionTemplate) {
            DescriptionTemplate.TEMPERATURE -> {
                "${extremePrefix}Temperatura: $valueStr ${config.unit}"
            }
            DescriptionTemplate.PEOPLE_COUNT -> {
                "${extremePrefix}Število ljudi: ${value.toInt()}"
            }
            DescriptionTemplate.HUMIDITY -> {
                "${extremePrefix}Vlažnost: $valueStr ${config.unit}"
            }
            DescriptionTemplate.STOCK_LEVEL -> {
                "${extremePrefix}Zaloga: ${value.toInt()} ${config.unit}"
            }
            DescriptionTemplate.CUSTOM -> {
                "${extremePrefix}${config.name}: $valueStr ${config.unit}"
            }
        }
    }

    /**
     * Clean up
     */
    fun cleanup() {
        stopAllSimulations()
        scope.cancel()
    }
}

/**
 * Simulation configuration
 */
data class SimulationConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val topic: String,
    val eventType: EventType,
    val minValue: Double,
    val maxValue: Double,
    val unit: String,
    val intervalMinutes: Int,
    val location: LocationData? = null,
    val manualLocation: ManualLocation? = null,
    val valueType: ValueType = ValueType.RANDOM,
    val descriptionTemplate: DescriptionTemplate = DescriptionTemplate.CUSTOM,
    val extremeThreshold: Double? = null,
    val extremeCondition: ExtremeCondition? = null,
    val customMetadata: Map<String, Any> = emptyMap(),
    val enabled: Boolean = true
)

/**
 * Manual location input
 */
data class ManualLocation(
    val address: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * Value generation type
 */
enum class ValueType {
    RANDOM,
    SINE_WAVE,
    LINEAR_INCREASE,
    LINEAR_DECREASE
}

/**
 * Description templates
 */
enum class DescriptionTemplate(val displayName: String) {
    TEMPERATURE("Temperatura"),
    PEOPLE_COUNT("Število ljudi"),
    HUMIDITY("Vlažnost"),
    STOCK_LEVEL("Zaloga"),
    CUSTOM("Poljubno")
}

/**
 * Extreme event conditions
 */
enum class ExtremeCondition {
    GREATER_THAN,
    LESS_THAN,
    EQUALS
}

/**
 * Predefined simulation templates
 */
object SimulationTemplates {
    fun getStoreCapacityTemplate(): SimulationConfig {
        return SimulationConfig(
            name = "Število ljudi v trgovini",
            topic = "store/capacity/people_count",
            eventType = EventType.USER_ACTION,
            minValue = 0.0,
            maxValue = 100.0,
            unit = "ljudi",
            intervalMinutes = 5,
            valueType = ValueType.RANDOM,
            descriptionTemplate = DescriptionTemplate.PEOPLE_COUNT,
            extremeThreshold = 50.0,
            extremeCondition = ExtremeCondition.GREATER_THAN,
            customMetadata = mapOf(
                "location_type" to "store",
                "alert_enabled" to true
            )
        )
    }

    fun getTemperatureTemplate(): SimulationConfig {
        return SimulationConfig(
            name = "Temperature",
            topic = "sensor/temperature",
            eventType = EventType.WEATHER_TEMPERATURE,
            minValue = -20.0,
            maxValue = 36.0,
            unit = "°C",
            intervalMinutes = 10,
            valueType = ValueType.SINE_WAVE,
            descriptionTemplate = DescriptionTemplate.TEMPERATURE,
            extremeThreshold = 30.0,
            extremeCondition = ExtremeCondition.GREATER_THAN,
            manualLocation = ManualLocation(
                address = "Koroška cesta 46, Maribor",
                latitude = 46.5547,
                longitude = 15.6466
            )
        )
    }

    fun getHumidityTemplate(): SimulationConfig {
        return SimulationConfig(
            name = "Humidity",
            topic = "sensor/humidity",
            eventType = EventType.WEATHER_HUMIDITY,
            minValue = 20.0,
            maxValue = 90.0,
            unit = "%",
            intervalMinutes = 15,
            valueType = ValueType.RANDOM,
            descriptionTemplate = DescriptionTemplate.HUMIDITY,
            extremeThreshold = 80.0,
            extremeCondition = ExtremeCondition.GREATER_THAN
        )
    }

    fun getStockLevelTemplate(): SimulationConfig {
        return SimulationConfig(
            name = "Zaloga artiklov",
            topic = "warehouse/stock/level",
            eventType = EventType.INVENTORY_UPDATE,
            minValue = 0.0,
            maxValue = 500.0,
            unit = "artiklov",
            intervalMinutes = 30,
            valueType = ValueType.LINEAR_DECREASE,
            descriptionTemplate = DescriptionTemplate.STOCK_LEVEL,
            extremeThreshold = 50.0,
            extremeCondition = ExtremeCondition.LESS_THAN,
            customMetadata = mapOf(
                "warehouse_id" to "WH-001"
            )
        )
    }
}

