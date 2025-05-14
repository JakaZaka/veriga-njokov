const mongoose = require('mongoose');

const clothingStoreSchema = mongoose.Schema(
  {
    name: {
      type: String,
      required: true,
    },
    website: {
      type: String,
    },
    // Location fields - similar to User model
    location: {
      address: {
        type: String,
      },
      city: {
        type: String,
      },
      country: {
        type: String,
        default: 'Slovenia',
      },
      coordinates: {
        // GeoJSON format for MongoDB geospatial queries
        type: {
          type: String,
          enum: ['Point'],
          default: 'Point',
        },
        coordinates: {
          // [longitude, latitude]
          type: [Number],
          required: true,
        }
      },
    },
    // Opening hours
    openingHours: [
      {
        day: {
          type: String,
          enum: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'],
          required: true,
        },
        openTime: {
          type: String,  // Format: "HH:MM"
          required: true,
        },
        closeTime: {
          type: String,  // Format: "HH:MM"
          required: true,
        },
        isClosed: {
          type: Boolean,
          default: false,
        },
      },
    ],
    // Store items - references to ClothingItem model
    items: [
      {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'ClothingItem',
      },
    ],
    // Contact information
    contactInfo: {
      phoneNumber: {
        type: String,
      },
      email: {
        type: String,
      },
    },
    // Additional store metadata
    metadata: {
      type: mongoose.Schema.Types.Mixed,
    },
  },
  { timestamps: true }
);

// Geospatial index for efficient location-based queries
clothingStoreSchema.index({ 'location.coordinates': '2dsphere' });
// Index for searching by name
clothingStoreSchema.index({ name: 'text' });

const ClothingStore = mongoose.model('ClothingStore', clothingStoreSchema);
module.exports = ClothingStore;