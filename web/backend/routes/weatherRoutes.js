const express = require('express');
const { 
  getCurrentWeather,
  getClothingRecommendations,
  updateWeatherData
} = require('../controllers/weatherController');
const { protect } = require('../middleware/authMiddleware');

const router = express.Router();

// Get current weather data (public)
router.get('/current', getCurrentWeather);

// Update weather data manually (admin only)
router.post('/update', protect, updateWeatherData);

// Get clothing recommendations based on weather (requires auth)
router.get('/recommendations', protect, getClothingRecommendations);

module.exports = router;