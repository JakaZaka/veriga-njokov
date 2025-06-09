const { get } = require('mongoose');
const mongoose = require('mongoose');
const { notify } = require('../app');
const ClothingItem = require('../models/ClothingItem');
const User = require('../models/User');

// @desc    Get all clothing items or only user's items if ?mine=true
// @route   GET /api/clothing
// @access  Public (optionally filtered by user if authenticated)
const getClothingItems = async (req, res) => {
  try {
    let filter = {};
    // If ?mine=true and user is authenticated, filter by user
    if (req.query.mine === 'true' && req.user && req.user._id) {
      filter.user = req.user._id;
    }
    // Optionally add more filters here (category, etc.)

    const clothingItems = await ClothingItem.find(filter).populate('user', 'username');
    res.json(clothingItems);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Get a specific clothing item
// @route   GET /api/clothing/:id
// @access  Private
const getClothingItemById = async (req, res) => {
  try {
    const clothingItem = await ClothingItem.findOne({
      _id: req.params.id,
      //user: req.user._id,
    }).populate('user');

    if (!clothingItem) {
      return res.status(404).json({ message: 'Clothing item not found' });
    }

    res.json(clothingItem);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Create a new clothing item
// @route   POST /api/clothing
// @access  Private
const createClothingItem = async (req, res) => {
  try {
    const clothingItem = new ClothingItem({
      name: req.body.name,
      category: req.body.category,
      subCategory: req.body.subCategory,
      season: req.body.season,
      color: req.body.color,
      size: req.body.size,
      imageUrl: "/images/" + req.file.filename,
      notes: req.body.notes,
      user: req.user?._id || req.session.userId,
    });

    const createdClothingItem = await clothingItem.save();
    res.status(201).json(createdClothingItem);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};

// @desc    Update a clothing item
// @route   PUT /api/clothing/:id
// @access  Private
const updateClothingItem = async (req, res) => {
  try {
    const userId = req.user?._id || req.session.userId;
    const clothingItem = await ClothingItem.findOne({
      _id: req.params.id,
      user: userId,
    });

    if (!clothingItem) {
      return res.status(404).json({ message: 'Clothing item not found' });
    }

    const updatableFields = ['wantToGive', 'name', 'category', 'subCategory', 'color', 'size', 'season', 'notes', 'imageUrl'];
    updatableFields.forEach((key) => {
      if (key in req.body) {
        clothingItem[key] = req.body[key];
      }
    });

    const updatedClothingItem = await clothingItem.save();
    res.json(updatedClothingItem);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};

// @desc    Delete a clothing item
// @route   DELETE /api/clothing/:id
// @access  Private
const deleteClothingItem = async (req, res) => {
  try {
    if (!req.user || !req.user._id) {
      return res.status(401).json({ message: 'Not authenticated' });
    }
    const item = await ClothingItem.findById(req.params.id);
    if (!item) {
      return res.status(404).json({ message: 'Clothing item not found' });
    }
    if (String(item.user) !== String(req.user._id)) {
      return res.status(403).json({ message: 'Not authorized to delete this item' });
    }
    await item.deleteOne();
    res.json({ message: 'Clothing item removed' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Toggle favorite status of a clothing item
// @route   PUT /api/clothing/:id/favorite
// @access  Private
const favoriteClothingItem = async (req, res) => {
  try {
    const userId = req.user?._id || req.session.userId;
    //console.log(userId);
    if (!userId) {
      return res.status(401).json({ message: "User not authenticated" });
    }
    const { want } = req.body;
    //console.log(want);
    const item = await ClothingItem.findById(req.params.id);
    //console.log(item);
    if (!item) return res.status(404).json({ message: "Item not found" });

    if (!Array.isArray(item.likedBy)) item.likedBy = [];
    //console.log(item.wantToGet);
   

    if (want) {
      if (!item.likedBy.some(User => User.equals(userId))) {
        item.likedBy.push(userId);
        //console.log(item.wantToGet);
      }
    } else {
      item.likedBy = item.likedBy.filter(User => !User.equals(userId));
    }
    await item.save();
    res.json(item);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};

// @desc    Increment wear count of a clothing item
// @route   PUT /api/clothing/:id/wear
// @access  Private
const incrementWearCount = async (req, res) => {
  try {
    const clothingItem = await ClothingItem.findOne({
      _id: req.params.id,
      user: req.user._id,
    });

    if (!clothingItem) {
      return res.status(404).json({ message: 'Clothing item not found' });
    }

    clothingItem.wearCount += 1;
    const updatedClothingItem = await clothingItem.save();
    
    res.json(updatedClothingItem);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};

const getClosetStats = async (req, res) => {
  try {
    const clothingItems = await ClothingItem.find({ user: req.session.userId });

    const occasionTypes = ['tops', 'bottoms', 'dresses', 'outerwear', 'shoes', 'accessories', 'other'];

   
    const trendMap = Object.fromEntries(occasionTypes.map(type => [type, 0]));

    clothingItems.forEach(item => {
      const category = item.category?.toLowerCase() || 'other';
      if (occasionTypes.includes(category)) {
        trendMap[category]++;
      } else {
        trendMap["other"]++;
      }
    });

    
    const clothingArray = Object.entries(trendMap).map(([category, value]) => ({
      category,
      value
    }));

    res.json(clothingArray);
  } catch (error) {
    console.error("Error building trend data:", error);
    res.status(500).json({ message: error.message });
  }
};

// @desc    Toggle wantToGet for a clothing item
// @route   POST /api/clothing/:id/wantToGet
// @access  Private
const toggleWantToGet = async (req, res) => {
  try {
    const userId = req.user?._id || req.session.userId;
    //console.log(userId);
    if (!userId) {
      return res.status(401).json({ message: "User not authenticated" });
    }
    const { want } = req.body;
    //console.log(want);
    const item = await ClothingItem.findById(req.params.id);
    //console.log(item);
    if (!item) return res.status(404).json({ message: "Item not found" });

    if (!Array.isArray(item.wantToGet)) item.wantToGet = [];
    //console.log(item.wantToGet);
   

    if (want) {
      if (!item.wantToGet.some(User => User.equals(userId))) {
        item.wantToGet.push(userId);
        //console.log(item.wantToGet);
      }
    } else {
      item.wantToGet = item.wantToGet.filter(User => !User.equals(userId));
    }
    await item.save();
    res.json(item);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};

const transferItem = async (req, res) => {
   try {
    const clothingItem = await ClothingItem.findById(req.params.clothingId);

    if (!clothingItem) {
      return res.status(404).json({ message: "Clothing item not found" });
    }

    clothingItem.user = req.params.newUserId;
    clothingItem.wantToGive = false;
    clothingItem.wantToGet = [];

    const updateClothingItem = await clothingItem.save();

    const io = req.app.get('io');
    const connectedUsers = req.app.get('connectedUsers');

    const user = await User.findById(req.session.userId);

    const recipientSocketId = connectedUsers.get(req.params.newUserId);
    if (recipientSocketId) {
      console.log(`Emitting 'clothingItemTransferred' to socket ${recipientSocketId}`);
      io.to(recipientSocketId).emit('clothingItemTransferred', {
        itemName: updateClothingItem.name,
        imageUrl: updateClothingItem.imageUrl,
        message: `You have received ${updateClothingItem.name} from ${user.username}! 🎊`
      });
    }

    res.json(updateClothingItem);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Create a new clothing item from Desktop App
// @route   POST /api/desktop-admin/clothingItems
// @access  Public (for desktop app)
const createClothingItemFromDesktop = async (req, res) => {
  try {
    console.log("Desktop app creating clothing item:", req.body);
    let { name, category, color, size, season, imageUrl, notes } = req.body;

    // Basic validation
    if (!name || !category) {
      return res.status(400).json({ 
        success: false, 
        error: 'Name and category are required.' 
      });
    }

    // Convert category enum to lowercase string to match MongoDB schema
    // DRESSES -> dresses, TOPS -> tops, etc.
    category = category.toLowerCase();
    
    // Convert season array of enums to lowercase strings
    // [SUMMER, FALL] -> [summer, fall]
    if (Array.isArray(season)) {
      season = season.map(s => s.toLowerCase());
    } else if (season) {
      // If not an array but a single value
      season = [season.toLowerCase()];
    } else {
      season = [];
    }

    // Create the clothing item with the properly formatted data
    const clothingItem = new ClothingItem({
      name,
      category,
      color,
      size,
      season,
      imageUrl, // Direct URL instead of file upload
      notes,
      fromShop: true // Mark as from shop since it's from scraper
    });

    const createdClothingItem = await clothingItem.save();
    console.log("Item created successfully:", createdClothingItem._id);
    
    // Return the response in the format expected by the desktop app
    res.status(201).json({ 
      success: true, 
      data: createdClothingItem, 
      message: "Clothing item created successfully from desktop app." 
    });
  } catch (error) {
    console.error('Error creating clothing item from desktop:', error);
    res.status(400).json({ 
      success: false, 
      error: error.message 
    });
  }
};

// @desc    Get all clothing items (for desktop app)
// @route   GET /api/desktop-admin/clothingItems
// @access  Public (for desktop app)
const getClothingItemsForDesktop = async (req, res) => {
  try {
    const clothingItems = await ClothingItem.find();
    res.json({ 
      success: true, 
      data: clothingItems 
    });
  } catch (error) {
    console.error('Error getting clothing items for desktop:', error);
    res.status(500).json({ 
      success: false, 
      error: error.message 
    });
  }
};

// Add these to the module.exports
module.exports = {
  getClothingItems,
  getClothingItemById,
  createClothingItem,
  updateClothingItem,
  deleteClothingItem,
  favoriteClothingItem,
  incrementWearCount,
  getClosetStats,
  transferItem,
  toggleWantToGet,
  createClothingItemFromDesktop,
  getClothingItemsForDesktop
} ;