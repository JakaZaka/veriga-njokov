const LocationHistory = require('../models/LocationHistory');

/**
 * @desc    Save new GPS location
 * @route   POST /api/location-history
 * @access  Public
 */
const saveLocation = async (req, res) => {
  try {
    const { timestamp, location, userId, deviceId } = req.body;

    // Validate location
    if (!location || !location.latitude || !location.longitude) {
      return res.status(400).json({ message: 'Location with latitude and longitude is required' });
    }

    const locationDate = timestamp ? new Date(timestamp) : new Date();

    const locationEntry = new LocationHistory({
      timestamp: locationDate,
      location: {
        latitude: location.latitude,
        longitude: location.longitude,
        altitude: location.altitude,
        accuracy: location.accuracy,
      },
      userId,
      deviceId,
    });

    const savedLocation = await locationEntry.save();
    res.status(201).json(savedLocation);
  } catch (error) {
    console.error('Error saving location:', error);
    res.status(400).json({ message: error.message });
  }
};

/**
 * @desc    Get location history with optional filters
 * @route   GET /api/location-history
 * @access  Public
 */
const getLocationHistory = async (req, res) => {
  try {
    const { userId, deviceId, startDate, endDate, limit } = req.query;

    // Build filter
    const filter = {};

    if (userId) {
      filter.userId = userId;
    }
    if (deviceId) {
      filter.deviceId = deviceId;
    }

    // Date range filter
    if (startDate || endDate) {
      filter.timestamp = {};
      if (startDate) {
        filter.timestamp.$gte = new Date(startDate);
      }
      if (endDate) {
        filter.timestamp.$lte = new Date(endDate);
      }
    }

    const queryLimit = limit ? parseInt(limit) : 100;

    const locations = await LocationHistory.find(filter)
      .sort({ timestamp: -1 })
      .limit(queryLimit);

    res.json({
      count: locations.length,
      locations,
    });
  } catch (error) {
    console.error('Error fetching location history:', error);
    res.status(500).json({ message: error.message });
  }
};

/**
 * @desc    Get latest location for a user/device
 * @route   GET /api/location-history/latest
 * @access  Public
 */
const getLatestLocation = async (req, res) => {
  try {
    const { userId, deviceId } = req.query;

    if (!userId && !deviceId) {
      return res.status(400).json({ message: 'userId or deviceId is required' });
    }

    const filter = {};
    if (userId) filter.userId = userId;
    if (deviceId) filter.deviceId = deviceId;

    const location = await LocationHistory.findOne(filter).sort({ timestamp: -1 });

    if (!location) {
      return res.status(404).json({ message: 'No location found' });
    }

    res.json(location);
  } catch (error) {
    console.error('Error fetching latest location:', error);
    res.status(500).json({ message: error.message });
  }
};

/**
 * @desc    Delete old location history (cleanup)
 * @route   DELETE /api/location-history/cleanup
 * @access  Public
 */
const cleanupOldLocations = async (req, res) => {
  try {
    const { days } = req.query;
    const daysBack = days ? parseInt(days) : 30;

    const cutoffDate = new Date();
    cutoffDate.setDate(cutoffDate.getDate() - daysBack);

    const result = await LocationHistory.deleteMany({
      timestamp: { $lt: cutoffDate },
    });

    res.json({
      message: `Deleted location history older than ${daysBack} days`,
      deletedCount: result.deletedCount,
    });
  } catch (error) {
    console.error('Error cleaning up location history:', error);
    res.status(500).json({ message: error.message });
  }
};

module.exports = {
  saveLocation,
  getLocationHistory,
  getLatestLocation,
  cleanupOldLocations,
};
