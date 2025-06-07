const express = require('express');
const { 
  registerUser, 
  loginUser, 
  nearbyUsers,
  getUserProfile,
  updateUserProfile,
  deleteUser,
  getSalesPerDistrict,
  getRequests
} = require('../controllers/userController');
const { protect } = require('../middleware/authMiddleware');

const router = express.Router();

// Public routes
router.post('/', registerUser);
router.post('/login', loginUser);
router.get('/nearby', nearbyUsers); 
router.get('/districtSales', getSalesPerDistrict);
router.get('/requests', getRequests);

// Protected routes
router.get('/profile', protect, getUserProfile);
router.put('/profile', protect, updateUserProfile);
router.put('/profile', protect, updateUserProfile);
router.delete('/', protect, deleteUser);

module.exports = router;