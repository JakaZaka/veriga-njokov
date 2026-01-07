const SensorEvent = require('../models/SensorEvent');

// @desc    Create a new sensor event
// @route   POST /api/events
// @access  Public
const createEvent = async (req, res) => {
  try {
    const { eventId, timestamp, topic, location, eventType, title, description, metadata } = req.body;

    // Convert timestamp from milliseconds to Date if needed
    const eventDate = timestamp ? new Date(timestamp) : new Date();

    const event = new SensorEvent({
      eventId,
      timestamp: eventDate,
      topic,
      location,
      eventType,
      title,
      description,
      metadata,
    });

    const savedEvent = await event.save();
    res.status(201).json(savedEvent);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};

module.exports = {
  createEvent,
};
