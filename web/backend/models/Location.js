const mongoose = require('mongoose');

const locationSchema = mongoose.Schema(
  {
    clothingStoreId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'ClothingStore',
    },
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
  }
);

// geospatial index for efficient location-based queries
locationSchema.index({ 'location.coordinates': '2dsphere' });

const Location = mongoose.model('Location', locationSchema);
module.exports = Location;;