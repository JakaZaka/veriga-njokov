/**
 * Express Routes za Clothing Classification
 * ==========================================
 * 
 * Uporaba v app.js:
 * const clothingRoutes = require('./routes/clothingRoutes');
 * app.use('/api/clothing', clothingRoutes);
 */

const express = require('express');
const router = express.Router();
const multer = require('multer');
const path = require('path');
const {
  classifyClothing,
  createClothingItemWithAI,
  updateClothingItem,
  checkPythonAPIHealth
} = require('../controllers/clothingController');

// Middleware za authentication (prilagodi glede na tvoj sistem)
const { protect } = require('../middleware/authMiddleware');

// Multer configuration za upload slik
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, 'uploads/'); // Prilagodi pot
  },
  filename: function (req, file, cb) {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    cb(null, uniqueSuffix + path.extname(file.originalname));
  }
});

const fileFilter = (req, file, cb) => {
  // Sprejmi samo slike
  if (file.mimetype.startsWith('image/')) {
    cb(null, true);
  } else {
    cb(new Error('Only image files are allowed'), false);
  }
};

const upload = multer({
  storage: storage,
  fileFilter: fileFilter,
  limits: {
    fileSize: 10 * 1024 * 1024 // 10MB limit
  }
});

// Routes

/**
 * @route   POST /api/clothing/classify
 * @desc    Klasificiraj sliko oblačila (samo AI, ne shrani v DB)
 * @access  Public ali Protected (odvisno od tvojega sistema)
 */
router.post('/classify', upload.single('image'), classifyClothing);

/**
 * @route   POST /api/clothing/create-with-ai
 * @desc    Ustvari clothing item z AI klasifikacijo
 * @access  Protected
 */
router.post('/create-with-ai', protect, upload.single('image'), createClothingItemWithAI);

/**
 * @route   GET /api/clothing/health
 * @desc    Preveri status Python API
 * @access  Public
 */
router.get('/health', checkPythonAPIHealth);

/**
 * @route   GET /api/clothing/node-health
 * @desc    Preveri status Node backend
 * @access  Public
 */
router.get('/node-health', (req, res) => {
  res.json({ status: 'ok' });
});



/**
 * @route   PUT /api/clothing/:id
 * @desc    Posodobi clothing item (user corrections)
 * @access  Protected
 * Pomembno: ta route mora biti na koncu, da ne ujame /health ali /node-health!
 */
router.put('/:id', protect, updateClothingItem);



module.exports = router;