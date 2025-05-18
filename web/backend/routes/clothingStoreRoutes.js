const express = require('express');
const { 
  getClothingStores,
  getClothingStoreById,
  createClothingStore,
  updateClothingStore,
  deleteClothingStore,
  getStoreItems,
  getNearbyStores,
  getExistingStores,
} = require('../controllers/clothingStoreController');
//const { protect, admin } = require('../middleware/authMiddleware');

const router = express.Router();

// Public routes
router.get('/', getClothingStores);
router.get('/existing', getExistingStores);
router.get('/nearby', getNearbyStores);
router.get('/:id', getClothingStoreById);
router.get('/:id/items', getStoreItems);

// Protected routes - Admin only
router.post('/', /*protect, admin,*/ createClothingStore);
router.put('/:id', /*protect, admin,*/ updateClothingStore);
router.delete('/:id', /*protect, admin,*/ deleteClothingStore);

module.exports = router;