const ClothingStore = require('../models/ClothingStore');
const ClothingItem = require('../models/ClothingItem');

// @desc    Get all clothing stores
// @route   GET /api/stores
// @access  Public
const getClothingStores = async (req, res) => {
  try {
    const filter = {};
    
    // Handle query parameters for filtering
    if (req.query.name) filter.name = { $regex: req.query.name, $options: 'i' };
    if (req.query.city) filter['location.city'] = { $regex: req.query.city, $options: 'i' };
    
    const clothingStores = await ClothingStore.find(filter);
    res.json(clothingStores);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

const getExistingStores = async (req, res) => {
  try {
     const stores = await ClothingStore.find({}, 'name _id'); 
      res.json(stores);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Get a specific clothing store
// @route   GET /api/stores/:id
// @access  Public
const getClothingStoreById = async (req, res) => {
  try {
    const clothingStore = await ClothingStore.findById(req.params.id);

    if (!clothingStore) {
      return res.status(404).json({ message: 'Clothing store not found' });
    }

    res.json(clothingStore);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Create a new clothing store
// @route   POST /api/stores
// @access  Private/Admin
const createClothingStore = async (req, res) => {
  try {
    const clothingStore = new ClothingStore(req.body);
    const createdClothingStore = await clothingStore.save();
    res.status(201).json(createdClothingStore);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};

// @desc    Update a clothing store
// @route   PUT /api/stores/:id
// @access  Private/Admin
const updateClothingStore = async (req, res) => {
  try {
    const clothingStore = await ClothingStore.findById(req.params.id);

    if (!clothingStore) {
      return res.status(404).json({ message: 'Clothing store not found' });
    }

    // Update fields
    Object.keys(req.body).forEach((key) => {
      clothingStore[key] = req.body[key];
    });

    const updatedClothingStore = await clothingStore.save();
    res.json(updatedClothingStore);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};

// @desc    Delete a clothing store
// @route   DELETE /api/stores/:id
// @access  Private/Admin
const deleteClothingStore = async (req, res) => {
  try {
    const clothingStore = await ClothingStore.findById(req.params.id);

    if (!clothingStore) {
      return res.status(404).json({ message: 'Clothing store not found' });
    }

    await clothingStore.remove();
    res.json({ message: 'Clothing store removed' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Get all items from a specific store
// @route   GET /api/stores/:id/items
// @access  Public
const getStoreItems = async (req, res) => {
  try {
    const clothingStore = await ClothingStore.findById(req.params.id);

    if (!clothingStore) {
      return res.status(404).json({ message: 'Clothing store not found' });
    }
    
    // Find all items associated with this store
    const items = await ClothingItem.find({
      _id: { $in: clothingStore.items }
    });
    
    res.json(items);
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
  getClothingStores,
  getClothingStoreById,
  createClothingStore,
  updateClothingStore,
  deleteClothingStore,
  getStoreItems,
  getNearbyStores,
  getExistingStores
};