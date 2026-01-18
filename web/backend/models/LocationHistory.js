const mongoose = require('mongoose');

const locationHistorySchema = mongoose.Schema(
  {
    timestamp: {
      type: Date,
      required: true,
      default: Date.now,
    },
    location: {
      latitude: {
        type: Number,
        required: true,
      },
      longitude: {
        type: Number,
        required: true,
      },
      altitude: {
        type: Number,
        required: false,
      },
      accuracy: {
        type: Number,
        required: false,
      },
    },
    userId: {
      type: String,
      required: false,
    },
    deviceId: {
      type: String,
      required: false,
    },
  },
  {
    timestamps: true, // Adds createdAt and updatedAt
  }
);

// Index for time-based queries (most recent first)
locationHistorySchema.index({ timestamp: -1 });

// Index for user-based queries
locationHistorySchema.index({ userId: 1, timestamp: -1 });

module.exports = mongoose.model('LocationHistory', locationHistorySchema);
