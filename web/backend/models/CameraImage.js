const mongoose = require('mongoose');

const cameraImageSchema = mongoose.Schema(
  {
    timestamp: {
      type: Date,
      required: true,
      default: Date.now,
    },
    location: {
      latitude: { type: Number, required: false },
      longitude: { type: Number, required: false },
      altitude: { type: Number, required: false },
      accuracy: { type: Number, required: false },
    },
    imageUrl: {
      type: String,
      required: true,
    },
    deviceId: {
      type: String,
      required: false,
    },
    userId: {
      type: String,
      required: false,
    },
  },
  {
    timestamps: true,
  }
);

cameraImageSchema.index({ timestamp: -1 });
cameraImageSchema.index({ deviceId: 1, timestamp: -1 });

module.exports = mongoose.model('CameraImage', cameraImageSchema);
