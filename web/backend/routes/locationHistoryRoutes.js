const express = require('express');
const {
  saveLocation,
  getLocationHistory,
  getLatestLocation,
  cleanupOldLocations,
} = require('../controllers/locationHistoryController');

const router = express.Router();

// GET /api/location-history/latest - Get latest location (must be before /:id)
router.get('/latest', getLatestLocation);

// POST /api/location-history - Save new GPS location
router.post('/', saveLocation);

// GET /api/location-history - Get location history with filters
router.get('/', getLocationHistory);

// DELETE /api/location-history/cleanup - Delete old location data
router.delete('/cleanup', cleanupOldLocations);

module.exports = router;
