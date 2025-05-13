const express = require('express');
const { 
  registerUser, 
  loginUser, 
  getUserProfile,
  updateUserProfile,
  deleteUser
} = require('../controllers/userController');
const { protect } = require('../middleware/authMiddleware');

const router = express.Router();

// Public routes
router.post('/register', registerUser);
router.post('/login', loginUser);

// Protected routes
router.get('/profile', protect, getUserProfile);
router.put('/profile', protect, updateUserProfile);
router.delete('/', protect, deleteUser);

module.exports = router;