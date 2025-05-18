const express = require('express');
const { 
  getLocations,
  getLocationById,
  createLocation,
  updateLocation,
  deleteLocation,
  getNearbyLocations,
} = require('../controllers/locationController');
const { protect, admin } = require('../middleware/authMiddleware');
const { get } = require('mongoose');

const router = express.Router();

// Public routes
router.get('/', getLocations);
//router.get('/nearby', getNearbyLocations);
router.get('/:id', getLocationById);

// Protected routes - Admin only
router.post('/', protect, admin, createLocation);
router.put('/:id', protect, admin, updateLocation);
router.delete('/:id', protect, admin, deleteLocation);

module.exports = router;