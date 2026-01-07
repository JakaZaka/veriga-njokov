const mongoose = require('mongoose');

const sensorEventSchema = mongoose.Schema(
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
      default: 'event',
    },
    title: {
      type: String,
    },
    description: {
      type: String,
      default: '',
    },
    metadata: {
      type: mongoose.Schema.Types.Mixed,
      default: {},
    },
  },
  {
    timestamps: true,
  }
);

module.exports = mongoose.model('SensorEvent', sensorEventSchema);
