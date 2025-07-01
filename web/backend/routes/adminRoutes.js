const express = require('express');
const { protect, admin } = require('../middleware/authMiddleware');
const User = require('../models/User');

const router = express.Router();

// @desc    Get all users (admin only)
// @route   GET /api/admin/users
// @access  Private/Admin
router.get('/users', protect, admin, async (req, res) => {
  try {
    const users = await User.find({}).select('-password');
    res.json(users);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

// @desc    Delete user (admin only)
// @route   DELETE /api/admin/users/:id
// @access  Private/Admin
router.delete('/users/:id', protect, admin, async (req, res) => {
  try {
    const user = await User.findById(req.params.id);
    
    if (!user) {
      return res.status(404).json({ message: 'User not found' });
    }
    
    // Prevent admin from deleting their own account
    if (user.role === 'admin' && req.user._id.toString() === user._id.toString()) {
      return res.status(400).json({ message: 'Cannot delete your own admin account' });
    }
    
    await User.findByIdAndDelete(req.params.id);
    res.json({ message: 'User deleted successfully' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

// @desc    Update user role (admin only)
// @route   PUT /api/admin/users/:id/role
// @access  Private/Admin
router.put('/users/:id/role', protect, admin, async (req, res) => {
  try {
    const { role } = req.body;
    
    if (!['user', 'admin'].includes(role)) {
      return res.status(400).json({ message: 'Invalid role' });
    }
    
    const user = await User.findById(req.params.id);
    
    if (!user) {
      return res.status(404).json({ message: 'User not found' });
    }
    
    user.role = role;
    await user.save();
    
    res.json({ 
      message: 'User role updated successfully', 
      user: { 
        _id: user._id,
        username: user.username,
        email: user.email,
        role: user.role
      } 
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

// @desc    Get system settings (placeholder)
// @route   GET /api/admin/settings
// @access  Private/Admin
router.get('/settings', protect, admin, (req, res) => {
  // Placeholder za nastavitve
  const settings = [
    {
      key: 'app_name',
      value: 'Closy',
      description: 'Application name',
      category: 'general',
      dataType: 'string',
      editable: true
    },
    {
      key: 'max_file_size',
      value: 5242880,
      description: 'Maximum file upload size in bytes',
      category: 'general',
      dataType: 'number',
      editable: true
    },
    {
      key: 'enable_notifications',
      value: true,
      description: 'Enable system notifications',
      category: 'notifications',
      dataType: 'boolean',
      editable: true
    }
  ];
  
  res.json(settings);
});

module.exports = router;