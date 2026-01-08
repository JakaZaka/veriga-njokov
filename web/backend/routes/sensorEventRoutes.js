const express = require('express');
const { createEvent, getEvents } = require('../controllers/sensorEventController');

const router = express.Router();

// POST /api/events - Create a new sensor event
router.post('/', createEvent);

// GET /api/events - Get all sensor events
router.get('/', getEvents);

module.exports = router;
