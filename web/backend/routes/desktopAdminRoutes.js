const express = require('express');
const User = require('../models/User');
const router = express.Router();

// Special endpoint for desktop admin app
// This bypasses normal authentication for admin operations

// Get all users (desktop admin only)
router.get('/users', async (req, res) => {
  try {
    const users = await User.find({}).select('-password');
    res.json({
      success: true,
      data: users,
      message: 'Users retrieved successfully'
    });
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      error: error.message 
    });
  }
});

// Delete user (desktop admin only)
router.delete('/users/:id', async (req, res) => {
  try {
    const user = await User.findById(req.params.id);
    
    if (!user) {
      return res.status(404).json({ 
        success: false,
        error: 'User not found' 
      });
    }
    
    await User.findByIdAndDelete(req.params.id);
    res.json({ 
      success: true,
      message: 'User deleted successfully'
    });
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      error: error.message 
    });
  }
});

// Update user (desktop admin only)
router.put('/users/:id', async (req, res) => {
  try {
    const user = await User.findById(req.params.id);
    
    if (!user) {
      return res.status(404).json({ 
        success: false,
        error: 'User not found' 
      });
    }
    
    // Update user fields from request body
    const { username, email, role, contactInfo, location } = req.body;
    
    if (username) user.username = username;
    if (email) user.email = email;
    if (role) user.role = role;
    if (contactInfo) user.contactInfo = contactInfo;
    if (location) user.location = location;
    
    // Don't update password this way
    
    const updatedUser = await user.save();
    
    res.json({
      success: true,
      data: updatedUser,
      message: 'User updated successfully'
    });
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      error: error.message 
    });
  }
});

module.exports = router;