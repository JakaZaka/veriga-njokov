const express = require('express');
const { createEvent } = require('../controllers/sensorEventController');

const router = express.Router();

// POST /api/events - Create a new sensor event
router.post('/', createEvent);

module.exports = router;
