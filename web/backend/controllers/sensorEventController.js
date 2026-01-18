const SensorEvent = require('../models/SensorEvent');
const { checkIfExtreme, createExtremeEvent } = require('./extremeEventController');
const ExtremeEvent = require('../models/ExtremeEvent');

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

    // Check if this is a manual extreme event
    // If source=manual and is_extreme=true, skip SensorEvent and save only to ExtremeEvent
    const isManualExtreme = metadata && metadata.source === 'manual' && metadata.is_extreme === true;

    if (isManualExtreme) {
      // Only save to ExtremeEvent collection
      try {
        const extremeEvent = new ExtremeEvent({
          eventId: finalEventId,
          timestamp: eventDate,
          topic,
          storeId: finalStoreId,
          location,
          eventType: finalEventType,
          title: title || 'Extreme Event',
          description,
          metadata,
          severity: metadata.severity || 'high',
          extremeReason: 'Manual extreme event created by user',
        });

        const savedExtremeEvent = await extremeEvent.save();
        console.log(`✅ Manual extreme event created (ExtremeEvent only): ${finalEventId}`);
        return res.status(201).json(savedExtremeEvent);
      } catch (error) {
        return res.status(400).json({ message: error.message });
      }
    }

    // Regular flow: save to SensorEvent first
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

    // Check if this event should also be saved as an extreme event
    // (e.g., simulated events with extreme values)
    const extremeCheck = checkIfExtreme(metadata, finalEventType || '', topic || '');
    
    if (extremeCheck.isExtreme) {
      try {
        // Create extreme event duplicate
        const extremeEvent = new ExtremeEvent({
          eventId: finalEventId,
          timestamp: eventDate,
          topic,
          storeId: finalStoreId,
          location,
          eventType: finalEventType,
          title: title || 'Extreme Event',
          description,
          metadata,
          severity: extremeCheck.severity || 'high',
          extremeReason: extremeCheck.reason,
          thresholdValue: extremeCheck.thresholdValue,
          actualValue: extremeCheck.actualValue,
        });

        await extremeEvent.save();
        console.log(`✅ Simulated extreme event created (both SensorEvent & ExtremeEvent): ${finalEventId}`);
      } catch (extremeError) {
        // Log error but don't fail the main event creation
        console.error('Error creating extreme event:', extremeError.message);
      }
    }

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

module.exports = {
  createEvent,
  getEvents,
};
