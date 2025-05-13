const mongoose = require('mongoose');

const weatherSchema = mongoose.Schema(
  {
    location: {
      type: String,
      required: true,
    },
    temperature: {
      type: Number,
      required: true,
    },
    isRaining: {
      type: Boolean,
      default: false,
    },
    fetchedAt: {
      type: Date,
      default: Date.now,
    }
  },
  { timestamps: true }
);

// Index for efficient lookups by location and time
weatherSchema.index({ location: 1, fetchedAt: -1 });

const Weather = mongoose.model('Weather', weatherSchema);
module.exports = Weather;