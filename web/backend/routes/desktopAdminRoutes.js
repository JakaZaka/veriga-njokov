const express = require('express');
const User = require('../models/User');
const router = express.Router();
const { createClothingItemFromDesktop, getClothingItemsForDesktop } = require('../controllers/clothingItemController');

// Special endpoint for desktop admin app
// This bypasses normal authentication for admin operations

// Get all users (desktop admin only)
router.get('/users', async (req, res) => {
  try {
    const users = await User.find({}).select('-password');
    res.json({
      success: true,
      data: users,
      message: 'Users retrieved successfully'
    });
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      error: error.message 
    });
  }
});

// Delete user (desktop admin only)
router.delete('/users/:id', async (req, res) => {
  try {
    const user = await User.findById(req.params.id);
    
    if (!user) {
      return res.status(404).json({ 
        success: false,
        error: 'User not found' 
      });
    }
    
    await User.findByIdAndDelete(req.params.id);
    res.json({ 
      success: true,
      message: 'User deleted successfully'
    });
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      error: error.message 
    });
  }
});

// Update user (desktop admin only)
router.put('/users/:id', async (req, res) => {
  try {
    const user = await User.findById(req.params.id);
    
    if (!user) {
      return res.status(404).json({ 
        success: false,
        error: 'User not found' 
      });
    }
    
    // Update user fields from request body
    const { username, email, role, contactInfo, location } = req.body;
    
    if (username) user.username = username;
    if (email) user.email = email;
    if (role) user.role = role;
    if (contactInfo) user.contactInfo = contactInfo;
    if (location) user.location = location;
    
    // Don't update password this way
    
    const updatedUser = await user.save();
    
    res.json({
      success: true,
      data: updatedUser,
      message: 'User updated successfully'
    });
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      error: error.message 
    });
  }
});

// Route for creating clothing items from desktop app
router.post('/clothingItems', createClothingItemFromDesktop);

// Route for fetching clothing items for desktop app
router.get('/clothingItems', getClothingItemsForDesktop);

// Delete clothing item (desktop admin only)
router.delete('/clothingItems/:id', async (req, res) => {
  try {
    const ClothingItem = require('../models/ClothingItem');
    const clothingItem = await ClothingItem.findById(req.params.id);
    
    if (!clothingItem) {
      return res.status(404).json({ 
        success: false,
        error: 'Clothing item not found' 
      });
    }
    
    await ClothingItem.findByIdAndDelete(req.params.id);
    res.json({ 
      success: true,
      message: 'Clothing item deleted successfully'
    });
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      error: error.message 
    });
  }
});

// Get store locations for desktop app
router.get('/locations', async (req, res) => {
  try {
    const Location = require('../models/Location');
    const locations = await Location.find().populate('clothingStoreId');
    
    res.json({
      success: true,
      data: locations
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

// Add DELETE endpoint for store locations
router.delete('/locations/:id', async (req, res) => {
  try {
    const Location = require('../models/Location');
    const location = await Location.findById(req.params.id);
    
    if (!location) {
      return res.status(404).json({ 
        success: false,
        error: 'Location not found' 
      });
    }
    
    await Location.findByIdAndDelete(req.params.id);
    res.json({ 
      success: true,
      message: 'Location deleted successfully'
    });
  } catch (error) {
    console.error('Error deleting location:', error);
    res.status(500).json({ 
      success: false, 
      error: error.message 
    });
  }
});

// Get outfits for desktop app
router.get('/outfits', async (req, res) => {
  try {
    const Outfit = require('../models/Outfit');
    
    // Don't populate the items - just return the references
    const outfits = await Outfit.find();
    
    res.json({
      success: true,
      data: outfits
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

// Delete outfit endpoint
router.delete('/outfits/:id', async (req, res) => {
  try {
    const Outfit = require('../models/Outfit');
    const outfit = await Outfit.findById(req.params.id);
    
    if (!outfit) {
      return res.status(404).json({ 
        success: false,
        error: 'Outfit not found' 
      });
    }
    
    await Outfit.findByIdAndDelete(req.params.id);
    res.json({ 
      success: true,
      message: 'Outfit deleted successfully'
    });
  } catch (error) {
    console.error('Error deleting outfit:', error);
    res.status(500).json({ 
      success: false, 
      error: error.message 
    });
  }
});

// Add this to your desktopAdminRoutes.js file
router.post('/outfits', async (req, res) => {
  try {
    // Import models at the top of your file or use here
    const Outfit = require('../models/Outfit');
    
    const { name, description, items, season, occasion, user } = req.body;
    
    // Create the outfit document
    const outfit = new Outfit({
      name,
      description,
      items, // These should now be properly formatted with item IDs
      season,
      occasion,
      user,
      liked: 0,
      likedBy: [],
      images: []
    });
    
    const savedOutfit = await outfit.save();
    
    res.status(201).json({
      success: true,
      data: savedOutfit,
      message: 'Outfit created successfully'
    });
  } catch (error) {
    console.error('Error creating outfit:', error);
    res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

module.exports = router;