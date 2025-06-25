const express = require('express');
const { 
  getClotnigItemEnums
} = require('../controllers/enumController');
const { protect, admin } = require('../middleware/authMiddleware');
const { get } = require('mongoose');

const router = express.Router();

// Public routes
router.get('/clothing', getClotnigItemEnums);


module.exports = router;