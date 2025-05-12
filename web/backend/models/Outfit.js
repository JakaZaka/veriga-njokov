const mongoose = require('mongoose');

const outfitSchema = mongoose.Schema(
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
    items: [
      {
        item: {
          type: mongoose.Schema.Types.ObjectId,
          ref: 'ClothingItem',
          required: true,
        },
        position: {
          type: String,
          enum: ['top', 'bottom', 'outer', 'shoes', 'accessory', 'other'],
        },
      },
    ],
    season: {
      type: [String],
      enum: ['spring', 'summer', 'fall', 'winter', 'all'],
    },
    occasion: {
      type: String,
    },
    favorite: {
      type: Boolean,
      default: false,
    },
    wearCount: {
      type: Number,
      default: 0,
    },
    lastWorn: {
      type: Date,
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
    // Flexible field for future extensions
    metadata: {
      type: mongoose.Schema.Types.Mixed,
    },
  },
  { timestamps: true }
);

outfitSchema.index({ user: 1, favorite: 1 });
outfitSchema.index({ user: 1, season: 1 });

const Outfit = mongoose.model('Outfit', outfitSchema);
module.exports = Outfit;