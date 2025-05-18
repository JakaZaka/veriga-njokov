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
    const outfit = new Outfit({
      ...req.body,
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
    Object.keys(req.body).forEach((key) => {
      outfit[key] = req.body[key];
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

const favoriteOutfit = async (req, res) => {
  try {
    const outfit = await Outfit.findById(req.params.id);
    if (!outfit) {
      return res.status(404).json({ message: 'Outfit not found' });
    }
    outfit.liked = !outfit.liked;
    const updatedOutfit = await outfit.save();
    res.json(updatedOutfit);
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

module.exports = {
  getOutfits,
  getOutfitById,
  createOutfit,
  updateOutfit,
  deleteOutfit,
  favoriteOutfit,
  wearOutfit,
};