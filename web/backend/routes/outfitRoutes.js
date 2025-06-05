const express = require('express');
const { 
  getOutfits,
  getOutfitById,
  createOutfit,
  updateOutfit,
  deleteOutfit,
  likeOutfit,
  wearOutfit
} = require('../controllers/outfitController');
const { protect } = require('../middleware/authMiddleware');

const router = express.Router();

// All routes are protected by default
//router.use(protect);

router.route('/')
  .get(getOutfits)
  .post(createOutfit);

  router.route('/:id')
  .get(getOutfitById)
  .put(updateOutfit)
  .delete(deleteOutfit);

router.post('/:id/like', likeOutfit);
//router.put('/:id/wear', wearOutfit);

module.exports = router;