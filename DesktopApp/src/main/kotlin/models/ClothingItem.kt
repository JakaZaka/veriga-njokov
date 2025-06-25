package models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class ClothingItem(
    @SerialName("_id") // Map MongoDB's _id to Kotlin's id
    val id: String? = null,
    val name: String,
    val category: ClothingCategory,
    val subCategory: String? = null,
    val color: String,
    val size: String,
    val season: List<Season>,
    val imageUrl: String? = null,
    val notes: String? = null,
    val liked: Boolean = false,
    val wearCount: Int = 0,
    val userId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable(with = ClothingCategorySerializer::class)
enum class ClothingCategory {
    TOPS, BOTTOMS, DRESSES, OUTERWEAR, SHOES, ACCESSORIES, OTHER;

    // Add this property
    val displayName: String
        get() = when(this) {
            TOPS -> "Tops"
            BOTTOMS -> "Bottoms" 
            DRESSES -> "Dresses"
            OUTERWEAR -> "Outerwear"
            SHOES -> "Shoes"
            ACCESSORIES -> "Accessories"
            OTHER -> "Other"
        }

    // Helper method to convert to lowercase for API requests
    fun toApiValue(): String = name.lowercase()
}

// Custom serializer for ClothingCategory
object ClothingCategorySerializer : KSerializer<ClothingCategory> {
    override val descriptor = PrimitiveSerialDescriptor("ClothingCategory", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ClothingCategory) {
        // When sending to API, convert to lowercase
        encoder.encodeString(value.name.lowercase())
    }

    override fun deserialize(decoder: Decoder): ClothingCategory {
        // When receiving from API, convert from lowercase to enum
        val value = decoder.decodeString()
        return ClothingCategory.values().find {
            it.name.equals(value, ignoreCase = true)
        } ?: throw IllegalArgumentException("Unknown category: $value")
    }
}

@Serializable(with = SeasonSerializer::class)
enum class Season {
    SPRING, SUMMER, FALL, WINTER, ALL;

    // Add this property
    val displayName: String
        get() = when(this) {
            SPRING -> "Spring"
            SUMMER -> "Summer"
            FALL -> "Fall"
            WINTER -> "Winter"
            ALL -> "All Seasons"
        }
    
    fun toApiValue(): String = name.lowercase()
}

object SeasonSerializer : KSerializer<Season> {
    override val descriptor = PrimitiveSerialDescriptor("Season", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Season) {
        encoder.encodeString(value.name.lowercase())
    }

    override fun deserialize(decoder: Decoder): Season {
        val value = decoder.decodeString()
        return Season.values().find {
            it.name.equals(value, ignoreCase = true)
        } ?: throw IllegalArgumentException("Unknown season: $value")
    }
}