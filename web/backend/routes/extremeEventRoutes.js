const express = require('express');
const {
  createExtremeEvent,
  getExtremeEvents,
  getExtremeEventById,
  acknowledgeExtremeEvent,
  resolveExtremeEvent,
  deleteExtremeEvent,
  getExtremeEventsStats,
} = require('../controllers/extremeEventController');

const router = express.Router();

// GET /api/extreme-events/stats - Get statistics (must be before /:id route)
router.get('/stats', getExtremeEventsStats);

// POST /api/extreme-events - Create a new extreme event
router.post('/', createExtremeEvent);

// GET /api/extreme-events - Get all extreme events with filters
router.get('/', getExtremeEvents);

// GET /api/extreme-events/:id - Get a single extreme event
router.get('/:id', getExtremeEventById);

// PATCH /api/extreme-events/:id/acknowledge - Acknowledge an extreme event
router.patch('/:id/acknowledge', acknowledgeExtremeEvent);

// PATCH /api/extreme-events/:id/resolve - Resolve an extreme event
router.patch('/:id/resolve', resolveExtremeEvent);

// DELETE /api/extreme-events/:id - Delete an extreme event
router.delete('/:id', deleteExtremeEvent);

module.exports = router;
