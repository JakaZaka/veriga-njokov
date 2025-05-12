const mongoose = require('mongoose');

const clothingItemSchema = mongoose.Schema(
  {
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true,
    },
    name: {
      type: String,
      required: true,
    },
    category: {
      type: String,
      required: true,
      enum: ['tops', 'bottoms', 'dresses', 'outerwear', 'shoes', 'accessories', 'other'],
    },
    subCategory: {
      type: String,
    },
    brand: {
      type: String,
    },
    color: {
      type: String,
    },
    size: {
      type: String,
    },
    season: {
      type: [String],
      enum: ['spring', 'summer', 'fall', 'winter', 'all'],
    },
    material: {
      type: String,
    },
    purchaseDate: {
      type: Date,
    },
    purchasePrice: {
      type: Number,
    },
    wearCount: {
      type: Number,
      default: 0,
    },
    favorite: {
      type: Boolean,
      default: false,
    },
    imageUrl: {
      type: String,
    },
    tags: {
      type: [String],
    },
    notes: {
      type: String,
    },
    status: {
      type: String,
      enum: ['active', 'donated', 'sold', 'archived'],
      default: 'active',
    },
    // Flexible field for future extensions
    metadata: {
      type: mongoose.Schema.Types.Mixed,
    },
  },
  { timestamps: true }
);

// Create indexes for common queries
clothingItemSchema.index({ user: 1, category: 1 });
clothingItemSchema.index({ user: 1, favorite: 1 });

const ClothingItem = mongoose.model('ClothingItem', clothingItemSchema);
module.exports = ClothingItem;