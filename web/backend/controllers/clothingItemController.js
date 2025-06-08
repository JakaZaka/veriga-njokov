const { get } = require('mongoose');
const mongoose = require('mongoose');
const { notify } = require('../app');
const ClothingItem = require('../models/ClothingItem');
const User = require('../models/User');

// @desc    Get all clothing items for a user
// @route   GET /api/clothing
// @access  Private
const getClothingItems = async (req, res) => {
  try {
    //const filter = { user: req.user._id };
    
    // Handle query parameters for filtering
    //if (req.query.category) filter.category = req.query.category;
    //if (req.query.favorite === 'true') filter.favorite = true;
    //if (req.query.status) filter.status = req.query.status;
    
    const clothingItems = await ClothingItem.find(/*filter*/).populate('user', 'username')
    var data = [];
    data.clothingItems = clothingItems;
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
    const clothingItem = await ClothingItem.findOne({
      _id: req.params.id,
      user: req.user._id,
    });

    if (!clothingItem) {
      return res.status(404).json({ message: 'Clothing item not found' });
    }

    await clothingItem.remove();
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
    const clothingItem = await ClothingItem.findOne({
      _id: req.params.id,
      user: req.user._id,
    });

    if (!clothingItem) {
      return res.status(404).json({ message: 'Clothing item not found' });
    }

    clothingItem.favorite = !clothingItem.favorite;
    const updatedClothingItem = await clothingItem.save();
    
    res.json(updatedClothingItem);
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
    if (!userId) {
      return res.status(401).json({ message: "User not authenticated" });
    }
    const { want } = req.body;
    const item = await ClothingItem.findById(req.params.id);
    if (!item) return res.status(404).json({ message: "Item not found" });

    if (!Array.isArray(item.wantToGet)) item.wantToGet = [];

    const userObjectId = mongoose.Types.ObjectId(userId);

    // Normalize wantToGet to ObjectId array
    item.wantToGet = item.wantToGet.map(id => mongoose.Types.ObjectId(id));

    if (want) {
      if (!item.wantToGet.some(id => id.equals(userObjectId))) {
        item.wantToGet.push(userObjectId);
      }
    } else {
      item.wantToGet = item.wantToGet.filter(id => !id.equals(userObjectId));
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
} ;