
const Outfit = require('../models/Outfit');

// @desc    Get all outfits for a user
// @route   GET /api/outfits
// @access  Private
const getOutfits = async (req, res) => {
  try {
    const outfits = await Outfit.find().populate('items.item');
    res.json(outfits);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Create an outfit
// @route   POST /api/outfits
// @access  Private
const createOutfit = async (req, res) => {
  try {
    if (!req.body.name || !req.body.items || !Array.isArray(req.body.items) || req.body.items.length === 0) {
      return res.status(400).json({ message: "Name and at least one clothing item are required." });
    }
    if (!req.session || !req.session.userId) {
      return res.status(401).json({ message: "User not authenticated." });
    }
    const outfit = new Outfit({
      ...req.body,
      user: req.session.userId, // <-- always set user here
      images: req.body.images || [],
    });
    const createdOutfit = await outfit.save();
    res.status(201).json(createdOutfit);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};


const getOutfitById = async (req, res) => {
  try {
    const outfit = await Outfit.findById(req.params.id).populate('items.item');
    if (!outfit) {
      return res.status(404).json({ message: 'Outfit not found' });
    }
    res.json(outfit);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

const updateOutfit = async (req, res) => {
  try {
    const outfit = await Outfit.findById(req.params.id);
    if (!outfit) {
      return res.status(404).json({ message: 'Outfit not found' });
    }
    // Prevent changing the user field
    Object.keys(req.body).forEach((key) => {
      if (key !== 'user') {
        outfit[key] = req.body[key];
      }
    });
    const updatedOutfit = await outfit.save();
    res.json(updatedOutfit);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};

const deleteOutfit = async (req, res) => {
  try {
    const outfit = await Outfit.findById(req.params.id);
    if (!outfit) {
      return res.status(404).json({ message: 'Outfit not found' });
    }
    await outfit.remove();
    res.json({ message: 'Outfit removed' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

const likeOutfit = async (req, res) => {
  if (!req.session || !req.session.userId) {
  return res.status(401).json({ message: 'User not authenticated' });
  }

  const userId = req.session.userId
  try {
    const outfit = await Outfit.findById(req.params.id);
    if (!outfit) {
      return res.status(404).json({ message: 'Outfit not found' });
    }

    const alreadyLiked = outfit.likedBy.includes(userId);
    console.log(`User ${userId} has already liked this outfit: ${alreadyLiked}`);
    if (alreadyLiked) {
      //console.log('LikedBy before filter:', outfit.likedBy.map(id => id.toString()));
      console.log('UserId:', userId);
      //const array = outfit.likedBy;
      outfit.likedBy.pull(userId);
      outfit.liked = Math.max(0, (outfit.liked || 1) - 1);
    } else {
      outfit.likedBy.push(userId);
      outfit.liked += 1;
    }
    await outfit.save();
    res.json(outfit);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};

const wearOutfit = async (req, res) => {
  try {
    const outfit = await Outfit.findById(req.params.id);
    if (!outfit) {
      return res.status(404).json({ message: 'Outfit not found' });
    }
    outfit.lastWorn = new Date();
    const updatedOutfit = await outfit.save();
    res.json(updatedOutfit);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};

const trendDataForChart = async (req, res) => {
  try {
    const outfits = await Outfit.find(); // optionally: `.where('user').equals(req.user._id)`
    
    const occasionTypes = ['casual', 'formal', 'sport', 'party', 'work', 'other'];
    const trendMap = {};

    outfits.forEach(outfit => {
      if (!outfit.createdAt) return;

      const dateKey = new Date(outfit.createdAt).toLocaleDateString("en-US", {
        month: "short",
        day: "numeric"
      });

      const occasion = outfit.occasion || "other";

      if (!trendMap[dateKey]) {
        trendMap[dateKey] = Object.fromEntries(occasionTypes.map(type => [type, 0]));
      }

      if (occasionTypes.includes(occasion)) {
        trendMap[dateKey][occasion]++;
      } else {
        trendMap[dateKey]["other"]++;
      }
    });

    const trendArray = Object.entries(trendMap).map(([date, counts]) => ({
      date,
      ...counts,
    }));

    // Optional: sort by date
    trendArray.sort((a, b) => {
      return new Date(a.date) - new Date(b.date);
    });

    res.json(trendArray);
  } catch (error) {
    console.error("Error building trend data:", error);
    res.status(500).json({ message: error.message });
  }
};


module.exports = {
  getOutfits,
  getOutfitById,
  createOutfit,
  updateOutfit,
  deleteOutfit,
  likeOutfit,
  wearOutfit,
  trendDataForChart,
};