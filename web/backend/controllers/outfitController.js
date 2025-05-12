const Outfit = require('../models/Outfit');

// @desc    Get all outfits for a user
// @route   GET /api/outfits
// @access  Private
const getOutfits = async (req, res) => {
  try {
    const outfits = await Outfit.find({ user: req.user._id })
      .populate('items.item');
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
    const outfit = new Outfit({
      ...req.body,
      user: req.user._id,
    });

    const createdOutfit = await outfit.save();
    res.status(201).json(createdOutfit);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};

// Additional controller methods for outfits
// (similar to clothing items for update, delete, etc.)

module.exports = {
  getOutfits,
  createOutfit,
  // Add other methods as needed
};