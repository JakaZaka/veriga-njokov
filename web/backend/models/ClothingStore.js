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
  },
  { timestamps: true }
);

// Geospatial index for efficient location-based queries
clothingStoreSchema.index({ 'location.coordinates': '2dsphere' });
// Index for searching by name
clothingStoreSchema.index({ name: 'text' });

const ClothingStore = mongoose.model('ClothingStore', clothingStoreSchema);
module.exports = ClothingStore;