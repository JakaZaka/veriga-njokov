const CameraImage = require('../models/CameraImage');
const path = require('path');
const fs = require('fs');

// Directory for storing uploaded images
const UPLOAD_DIR = path.join(__dirname, '../uploads/camera');
if (!fs.existsSync(UPLOAD_DIR)) {
  fs.mkdirSync(UPLOAD_DIR, { recursive: true });
}

/**
 * @desc    Save new camera image (base64)
 * @route   POST /api/camera-images
 * @access  Public
 * Body: { timestamp, location, imageBase64, deviceId, userId }
 */
const saveCameraImage = async (req, res) => {
  try {
    const { timestamp, location, imageBase64, deviceId, userId } = req.body;
    if (!imageBase64) {
      return res.status(400).json({ message: 'imageBase64 is required' });
    }
    // Decode base64 and save to file
    const buffer = Buffer.from(imageBase64, 'base64');
    const fileName = `img_${Date.now()}_${Math.floor(Math.random()*10000)}.jpg`;
    const filePath = path.join(UPLOAD_DIR, fileName);
    fs.writeFileSync(filePath, buffer);
    const imageUrl = `/uploads/camera/${fileName}`;

    const cameraImage = new CameraImage({
      timestamp: timestamp ? new Date(timestamp) : new Date(),
      location,
      imageUrl,
      deviceId,
      userId,
    });
    const saved = await cameraImage.save();
    res.status(201).json(saved);
  } catch (error) {
    console.error('Error saving camera image:', error);
    res.status(400).json({ message: error.message });
  }
};

/**
 * @desc    Get camera images (with filters)
 * @route   GET /api/camera-images
 * @access  Public
 */
const getCameraImages = async (req, res) => {
  try {
    const { deviceId, userId, startDate, endDate, limit } = req.query;
    const filter = {};
    if (deviceId) filter.deviceId = deviceId;
    if (userId) filter.userId = userId;
    if (startDate || endDate) {
      filter.timestamp = {};
      if (startDate) filter.timestamp.$gte = new Date(startDate);
      if (endDate) filter.timestamp.$lte = new Date(endDate);
    }
    const queryLimit = limit ? parseInt(limit) : 100;
    const images = await CameraImage.find(filter).sort({ timestamp: -1 }).limit(queryLimit);
    res.json({ count: images.length, images });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

module.exports = {
  saveCameraImage,
  getCameraImages,
};
