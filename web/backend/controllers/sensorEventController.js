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
      storeId, store_id,
      location, 
      eventType, event_type,
      title, 
      description, 
      metadata 
    } = req.body;

    // Use whichever format client sent
    const finalEventId = eventId || event_id;
    const finalEventType = eventType || event_type;
    const finalStoreId = storeId || store_id;

    // Convert timestamp from milliseconds to Date if needed
    const eventDate = timestamp ? new Date(timestamp) : new Date();

    const event = new SensorEvent({
      eventId: finalEventId,
      timestamp: eventDate,
      topic,
      storeId: finalStoreId,
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
    const { storeId, store_id } = req.query;
    
    // Build filter
    const filter = {};
    const finalStoreId = storeId || store_id;
    if (finalStoreId) {
      filter.storeId = finalStoreId;
    }
    
    const events = await SensorEvent.find(filter)
      .populate('storeId', 'name website')
      .sort({ timestamp: -1 });
    res.json(events);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// GET /api/events/latest
const getLatestOccupancy = async (req, res) => {
  try {
    const latest = await SensorEvent.aggregate([
      {
        $match: {
          storeId: { $exists: true, $ne: null }
        }
      },
      { $sort: { timestamp: -1 } },
      {
        $group: {
          _id: "$storeId",
          event: { $first: "$$ROOT" }
        }
      },
      {
        $project: {
          _id: 0,
          storeId: "$event.storeId",
          peopleCount: "$event.metadata.value"
        }
      }
    ]);

    res.json(latest);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

module.exports = {
  createEvent,
  getEvents,
  getLatestOccupancy
};
