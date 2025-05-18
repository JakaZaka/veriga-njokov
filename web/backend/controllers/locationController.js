const ClothingStore = require('../models/ClothingStore');
const Location = require('../models/Location');
const NodeGeocoder = require('node-geocoder');
const geocoder = NodeGeocoder({ provider: 'openstreetmap' });

async function geocodeAddress(address) {
  const apiKey = process.env.OPENCAGE_API_KEY;
  const res = await fetch(`https://api.opencagedata.com/geocode/v1/json?q=${encodeURIComponent(address)}&key=${apiKey}`);
  const data = await res.json();

  if (!data.results || data.results.length === 0) {
    throw new Error('Unable to geocode address');
  }

  const [lat, lng] = [
    data.results[0].geometry.lat,
    data.results[0].geometry.lng,
  ];
  return [lng, lat]; // GeoJSON format
}


// @desc    Get all clothing stores
// @route   GET /api/stores
// @access  Public
const getLocations = async (req, res) => {
  try {
    /*const locations = await Location.find();
    res.json(locations);*/

    const locations = await Location.find(/*filter*/).populate('clothingStoreId', 'name website');
    var data = [];
    data.locations = locations;
    res.json(locations);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Get a specific clothing store
// @route   GET /api/stores/:id
// @access  Public
const getLocationById = async (req, res) => {
  try {
    const location = await Location.findById(req.params.id);

    if (!location) {
      return res.status(404).json({ message: 'Location not found' });
    }

    res.json(location);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Create a new clothing store
// @route   POST /api/stores
// @access  Private/Admin
const createLocation = async (req, res) => {
  try {
   
    console.log("Request body:", req.body);
    const { address, city, country, clothingStoreId } = req.body;

    if (!address || !city || !country || !clothingStoreId) {
      return res.status(400).json({ message: 'Missing required fields' });
    }

    const fullAddress = `${address}, ${city}, ${country}`;
    console.log("Full address to geocode:", fullAddress);

    const geoRes = await geocoder.geocode(fullAddress);

    let coordinates = null;
    let geoCity = city;
    let geoCountry = country;

    if (geoRes && geoRes.length > 0) {
      const geo = geoRes[0];
      coordinates = {
        type: 'Point',
        coordinates: [geo.longitude, geo.latitude],
      };
      geoCity = geo.city || city;
      geoCountry = geo.country || country;
      console.log("Geocoded coordinates:", coordinates.coordinates);
    } else {
      console.warn("Geocoding failed. Proceeding without coordinates.");
    }

    const newLocation = new Location({
      address,
      city: geoCity,
      country: geoCountry,
      clothingStoreId,
      ...(coordinates && { coordinates })
    });

    const savedLocation = await newLocation.save();
    console.log("Location saved:", savedLocation);

    res.status(201).json(savedLocation);
  } catch (err) {
    console.error("Error in createLocation:", err);
    res.status(500).json({ message: 'Failed to add location', error: err.message });
  }
};

// @desc    Update a clothing store
// @route   PUT /api/stores/:id
// @access  Private/Admin
const updateLocation = async (req, res) => {
  try {
    const location = await Location.findById(req.params.id);

    if (!location) {
      return res.status(404).json({ message: 'Clothing store not found' });
    }

    // Update fields
    Object.keys(req.body).forEach((key) => {
      location[key] = req.body[key];
    });

    const updatedLocation = await location.save();
    res.json(updatedLocation);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};

// @desc    Delete a clothing store
// @route   DELETE /api/stores/:id
// @access  Private/Admin
const deleteLocation = async (req, res) => {
  try {
    const location = await Location.findById(req.params.id);

    if (!location) {
      return res.status(404).json({ message: 'Location store not found' });
    }

    await location.remove();
    res.json({ message: 'Location store removed' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};


// @desc    Get nearby stores based on coordinates
// @route   GET /api/stores/nearby
// @access  Public
const getNearbyStores = async (req, res) => {
  try {
    const { longitude, latitude, maxDistance = 10000 } = req.query; // maxDistance in meters, default 10km
    
    if (!longitude || !latitude) {
      return res.status(400).json({ message: 'Longitude and latitude are required' });
    }
    
    // Find stores within the specified radius
    const nearbyStores = await ClothingStore.find({
      'location.coordinates': {
        $near: {
          $geometry: {
            type: 'Point',
            coordinates: [parseFloat(longitude), parseFloat(latitude)],
          },
          $maxDistance: parseInt(maxDistance),
        },
      },
    });
    
    res.json(nearbyStores);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

module.exports = {
  getLocations,
  getLocationById,
  createLocation,
  updateLocation,
  deleteLocation,
  getNearbyStores,
};