const express = require('express');
const { 
  registerUser, 
  loginUser, 
  nearbyUsers,
  getUserProfile,
  updateUserProfile,
  deleteUser,
  getAllUsers
} = require('../controllers/userController');
const { protect } = require('../middleware/authMiddleware');

const router = express.Router();

// Public routes
router.post('/', registerUser);
router.post('/login', loginUser);
router.get('/nearby', nearbyUsers);
router.get('/', getAllUsers); // This is the critical route for getting all users

// Protected routes
router.get('/profile', protect, getUserProfile);
router.put('/profile', protect, updateUserProfile);
router.delete('/', protect, deleteUser);

module.exports = router;