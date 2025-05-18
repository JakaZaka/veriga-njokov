const ClothingItem = require('../models/ClothingItem');

// @desc    Get all clothing stores
// @route   GET /api/stores
// @access  Public
const getClotnigItemEnums = async (req, res) => {
  const categoryEnum = ClothingItem.schema.path('category').enumValues;
  const seasonEnum = ClothingItem.schema.path('season').caster.enumValues; // since it's [String]

  res.json({
    category: categoryEnum,
    season: seasonEnum,
  });
};



module.exports = {
  getClotnigItemEnums
};