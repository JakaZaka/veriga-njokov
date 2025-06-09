const express = require('express');
var multer = require('multer');
var upload = multer({ dest: 'public/images/' });
const { 
  getClothingItems,
  getClothingItemById,
  createClothingItem,
  updateClothingItem,
  deleteClothingItem,
  favoriteClothingItem,
  incrementWearCount,
  getClosetStats,
  toggleWantToGet,
  transferItem
} = require('../controllers/clothingItemController');
const { protect } = require('../middleware/authMiddleware');

const router = express.Router();

//router.use(protect);

router.route('/')
  .get(getClothingItems)
  .post(protect, upload.single('image'), createClothingItem);

router.get('/closetStats', getClosetStats); 
router.put('/transfer/:clothingId/:newUserId', transferItem);

router.route('/:id')
  .get(getClothingItemById)
  .put(protect, updateClothingItem)
  .delete(protect, deleteClothingItem);

router.post('/:id/favorite', favoriteClothingItem);
router.put('/:id/wear', incrementWearCount);
router.post('/:id/wantToGet', protect, toggleWantToGet);

module.exports = router;