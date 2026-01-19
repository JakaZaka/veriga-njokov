const express = require('express');
const { createEvent, getEvents, getLatestOccupancy } = require('../controllers/sensorEventController');
const { get } = require('mongoose');

const router = express.Router();

// POST /api/events - Create a new sensor event
router.post('/', createEvent);

// GET /api/events - Get all sensor events
router.get('/', getEvents);

// GET /api/events/latest
router.get('/latest', getLatestOccupancy);

module.exports = router;
