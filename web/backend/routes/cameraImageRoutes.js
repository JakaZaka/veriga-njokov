const express = require('express');
const { saveCameraImage, getCameraImages } = require('../controllers/cameraImageController');

const router = express.Router();

// POST /api/camera-images - Save new camera image (base64)
router.post('/', saveCameraImage);

// GET /api/camera-images - Get images (with filters)
router.get('/', getCameraImages);

module.exports = router;
