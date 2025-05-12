const Weather = require('../models/Weather');
const ClothingItem = require('../models/ClothingItem');

// @desc    Get current weather data
// @route   GET /api/weather/current
// @access  Public
const getCurrentWeather = async (req, res) => {
  try {
    const location = req.query.location || 'Ljubljana';
    
    // Get most recent weather data for the location
    const weatherData = await Weather.findOne({ 
      location 
    }).sort({ fetchedAt: -1 });
    
    if (!weatherData) {
      return res.status(404).json({ 
        message: 'No weather data available. Please update weather data.'
      });
    }
    
    res.json(weatherData);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Update weather data manually
// @route   POST /api/weather/update
// @access  Private
const updateWeatherData = async (req, res) => {
  try {
    const { location, temperature, isRaining } = req.body;
    
    if (!location || temperature === undefined) {
      return res.status(400).json({ 
        message: 'Location and temperature are required' 
      });
    }
    
    const weatherData = new Weather({
      location,
      temperature,
      isRaining: isRaining || false,
      fetchedAt: new Date()
    });
    
    const savedWeather = await weatherData.save();
    res.status(201).json(savedWeather);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};

// @desc    Get clothing recommendations based on weather
// @route   GET /api/weather/recommendations
// @access  Private
const getClothingRecommendations = async (req, res) => {
  try {
    const location = req.query.location || 'Ljubljana';
    const userId = req.user._id;
    
    // Get most recent weather data
    const weatherData = await Weather.findOne({ 
      location 
    }).sort({ fetchedAt: -1 });
    
    if (!weatherData) {
      return res.status(404).json({ 
        message: 'No weather data available. Please update weather data.'
      });
    }
    
    // Determine appropriate clothing based on temperature and rain
    const { temperature, isRaining } = weatherData;
    
    // Map temperature to season
    let recommendedSeason;
    if (temperature < 5) {
      recommendedSeason = 'winter';
    } else if (temperature < 15) {
      recommendedSeason = 'fall';
    } else if (temperature < 25) {
      recommendedSeason = 'spring';
    } else {
      recommendedSeason = 'summer';
    }
    
    // Build query for appropriate clothing
    const query = {
      user: userId,
      status: 'active',
      $or: [
        { season: recommendedSeason },
        { season: 'all' }
      ]
    };
    
    // Find appropriate clothing items
    const clothingItems = await ClothingItem.find(query);
    
    // If raining, suggest waterproof items
    let weatherMessage = '';
    if (isRaining) {
      const waterproofItems = await ClothingItem.find({
        user: userId,
        tags: { $in: ['waterproof', 'rain'] }
      });
      
      weatherMessage = "It's raining. Don't forget a waterproof jacket or an umbrella!";
      
      return res.json({
        weather: weatherData,
        recommendations: {
          regularItems: clothingItems,
          waterproofItems: waterproofItems,
          weatherMessage
        }
      });
    }
    
    // Generate weather message based on temperature
    if (temperature < 5) {
      weatherMessage = "It's very cold. Wear warm layers!";
    } else if (temperature < 15) {
      weatherMessage = "It's cool outside. Consider a jacket.";
    } else if (temperature < 25) {
      weatherMessage = "Pleasant temperature. Light layers recommended.";
    } else {
      weatherMessage = "It's warm! Dress lightly.";
    }
    
    // Return recommendations
    res.json({
      weather: weatherData,
      recommendations: {
        items: clothingItems,
        weatherMessage
      }
    });
    
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

module.exports = {
  getCurrentWeather,
  updateWeatherData,
  getClothingRecommendations
};