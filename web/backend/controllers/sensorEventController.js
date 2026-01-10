const SensorEvent = require('../models/SensorEvent');

// @desc    Create a new sensor event
// @route   POST /api/events
// @access  Public
const createEvent = async (req, res) => {
  try {
    // Support both camelCase and snake_case from client
    const { 
      eventId, event_id,
      timestamp, 
      topic, 
      location, 
      eventType, event_type,
      title, 
      description, 
      metadata 
    } = req.body;

    // Use whichever format client sent
    const finalEventId = eventId || event_id;
    const finalEventType = eventType || event_type;

    // Convert timestamp from milliseconds to Date if needed
    const eventDate = timestamp ? new Date(timestamp) : new Date();

    const event = new SensorEvent({
      eventId: finalEventId,
      timestamp: eventDate,
      topic,
      location,
      eventType: finalEventType,
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

// @desc    Get all sensor events
// @route   GET /api/events
// @access  Public
const getEvents = async (req, res) => {
  try {
    const events = await SensorEvent.find().sort({ timestamp: -1 });
    res.json(events);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

module.exports = {
  createEvent,
  getEvents,
};
