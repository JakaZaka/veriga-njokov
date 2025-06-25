const express = require('express');
const { 
  registerUser, 
  loginUser, 
  nearbyUsers,
  getUserProfile,
  updateUserProfile,
  deleteUser,
  getAllUsers,
  getSalesPerDistrict,
  getRequests,
  getAllUsersWithExtras
} = require('../controllers/userController');
const { protect } = require('../middleware/authMiddleware');

const router = express.Router();

// Public routes
router.post('/', registerUser);
router.post('/login', loginUser);
router.get('/nearby', nearbyUsers);
router.get('/', getAllUsers);
router.get('/nearby', nearbyUsers); 
router.get('/districtSales', getSalesPerDistrict);
router.get('/requests', getRequests);
router.get('/allWithExtras', getAllUsersWithExtras);

// Protected routes
router.get('/profile', protect, getUserProfile);
router.put('/profile', protect, updateUserProfile);
router.delete('/', protect, deleteUser);

module.exports = router;