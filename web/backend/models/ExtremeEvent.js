const mongoose = require('mongoose');

const extremeEventSchema = mongoose.Schema(
  {
    eventId: {
      type: String,
      required: true,
      unique: true,
    },
    timestamp: {
      type: Date,
      required: true,
      default: Date.now,
    },
    topic: {
      type: String,
      required: true,
    },
    storeId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'ClothingStore',
      required: false,
    },
    location: {
      latitude: {
        type: Number,
      },
      longitude: {
        type: Number,
      },
      altitude: {
        type: Number,
      },
      accuracy: {
        type: Number,
      },
    },
    eventType: {
      type: String,
      required: false,
      default: 'extreme_event',
    },
    title: {
      type: String,
      required: true,
    },
    description: {
      type: String,
      default: '',
    },
    metadata: {
      type: mongoose.Schema.Types.Mixed,
      default: {},
    },
    // Extreme event specific fields
    severity: {
      type: String,
      enum: ['low', 'medium', 'high', 'critical'],
      default: 'medium',
    },
    extremeReason: {
      type: String,
      required: false,
    },
    thresholdValue: {
      type: Number,
      required: false,
    },
    actualValue: {
      type: Number,
      required: false,
    },
    // Blockchain integration fields (for future use)
    blockchain: {
      stored: {
        type: Boolean,
        default: false,
      },
      blockHash: {
        type: String,
        required: false,
      },
      blockIndex: {
        type: Number,
        required: false,
      },
      minedAt: {
        type: Date,
        required: false,
      },
    },
    // Status tracking
    acknowledged: {
      type: Boolean,
      default: false,
    },
    acknowledgedBy: {
      type: String,
      required: false,
    },
    acknowledgedAt: {
      type: Date,
      required: false,
    },
    resolved: {
      type: Boolean,
      default: false,
    },
    resolvedAt: {
      type: Date,
      required: false,
    },
    notes: {
      type: String,
      required: false,
    },
  },
  {
    timestamps: true,
  }
);

// Indexes for better query performance
extremeEventSchema.index({ timestamp: -1 });
extremeEventSchema.index({ storeId: 1, timestamp: -1 });
extremeEventSchema.index({ eventType: 1 });
extremeEventSchema.index({ severity: 1 });
extremeEventSchema.index({ acknowledged: 1 });
extremeEventSchema.index({ resolved: 1 });

module.exports = mongoose.model('ExtremeEvent', extremeEventSchema);
