const express = require('express');
const { 
  getClothingItems,
  getClothingItemById,
  createClothingItem,
  updateClothingItem,
  deleteClothingItem,
  favoriteClothingItem,
  incrementWearCount
} = require('../controllers/clothingItemController');
const { protect } = require('../middleware/authMiddleware');

const router = express.Router();

router.use(protect);

router.route('/')
  .get(getClothingItems)
  .post(createClothingItem);

router.route('/:id')
  .get(getClothingItemById)
  .put(updateClothingItem)
  .delete(deleteClothingItem);

router.put('/:id/favorite', favoriteClothingItem);
router.put('/:id/wear', incrementWearCount);

module.exports = router;