const ExtremeEvent = require('../models/ExtremeEvent');

/**
 * Pravila za avtomatsko zaznavanje ekstremnih dogodkov
 * Lahko jih prilagodiš glede na potrebe
 */
const EXTREME_RULES = {
  people_count: { threshold: 90, condition: 'greater' },
  temperature: { threshold: 35, condition: 'greater' },
  humidity: { threshold: 85, condition: 'greater' },
  stock_level: { threshold: 10, condition: 'less' },
};

/**
 * Preveri ali je dogodek ekstremen na podlagi pravil
 */
const checkIfExtreme = (metadata, eventType, topic) => {
  // Če je Android že označil kot extreme
  if (metadata && metadata.is_extreme === true) {
    return {
      isExtreme: true,
      reason: 'Marked as extreme by client',
      severity: metadata.severity || 'high',
    };
  }

  // Avtomatska detekcija na podlagi vrednosti
  if (metadata && metadata.value !== undefined) {
    const value = parseFloat(metadata.value);

    // Preveri pravila glede na topic ali eventType
    for (const [key, rule] of Object.entries(EXTREME_RULES)) {
      if (topic.includes(key) || eventType.includes(key)) {
        const isExtreme =
          rule.condition === 'greater'
            ? value > rule.threshold
            : value < rule.threshold;

        if (isExtreme) {
          return {
            isExtreme: true,
            reason: `${key} value ${value} is ${rule.condition} than threshold ${rule.threshold}`,
            thresholdValue: rule.threshold,
            actualValue: value,
            severity: value > rule.threshold * 1.5 ? 'critical' : 'high',
          };
        }
      }
    }
  }

  return { isExtreme: false };
};

/**
 * @desc    Create a new extreme event
 * @route   POST /api/extreme-events
 * @access  Public
 */
const createExtremeEvent = async (req, res) => {
  try {
    const {
      eventId,
      event_id,
      timestamp,
      topic,
      storeId,
      store_id,
      location,
      eventType,
      event_type,
      title,
      description,
      metadata,
      severity,
      extremeReason,
      thresholdValue,
      actualValue,
    } = req.body;

    const finalEventId = eventId || event_id;
    const finalEventType = eventType || event_type;
    const finalStoreId = storeId || store_id;
    const eventDate = timestamp ? new Date(timestamp) : new Date();

    // Preveri ali dogodek že obstaja
    const existingEvent = await ExtremeEvent.findOne({ eventId: finalEventId });
    if (existingEvent) {
      return res.status(400).json({ message: 'Extreme event with this ID already exists' });
    }

    const extremeEvent = new ExtremeEvent({
      eventId: finalEventId,
      timestamp: eventDate,
      topic,
      storeId: finalStoreId,
      location,
      eventType: finalEventType,
      title,
      description,
      metadata,
      severity: severity || 'medium',
      extremeReason,
      thresholdValue,
      actualValue,
    });

    const savedEvent = await extremeEvent.save();
    res.status(201).json(savedEvent);
  } catch (error) {
    console.error('Error creating extreme event:', error);
    res.status(400).json({ message: error.message });
  }
};

/**
 * @desc    Get all extreme events with optional filters
 * @route   GET /api/extreme-events
 * @access  Public
 */
const getExtremeEvents = async (req, res) => {
  try {
    const {
      storeId,
      store_id,
      severity,
      acknowledged,
      resolved,
      limit,
      startDate,
      endDate,
    } = req.query;

    // Build filter
    const filter = {};
    const finalStoreId = storeId || store_id;

    if (finalStoreId) {
      filter.storeId = finalStoreId;
    }
    if (severity) {
      filter.severity = severity;
    }
    if (acknowledged !== undefined) {
      filter.acknowledged = acknowledged === 'true';
    }
    if (resolved !== undefined) {
      filter.resolved = resolved === 'true';
    }

    // Date range filter
    if (startDate || endDate) {
      filter.timestamp = {};
      if (startDate) {
        filter.timestamp.$gte = new Date(startDate);
      }
      if (endDate) {
        filter.timestamp.$lte = new Date(endDate);
      }
    }

    const queryLimit = limit ? parseInt(limit) : 100;

    const events = await ExtremeEvent.find(filter)
      .populate('storeId', 'name website address')
      .sort({ timestamp: -1 })
      .limit(queryLimit);

    res.json({
      count: events.length,
      events,
    });
  } catch (error) {
    console.error('Error fetching extreme events:', error);
    res.status(500).json({ message: error.message });
  }
};

/**
 * @desc    Get a single extreme event by ID
 * @route   GET /api/extreme-events/:id
 * @access  Public
 */
const getExtremeEventById = async (req, res) => {
  try {
    const event = await ExtremeEvent.findById(req.params.id).populate(
      'storeId',
      'name website address'
    );

    if (!event) {
      return res.status(404).json({ message: 'Extreme event not found' });
    }

    res.json(event);
  } catch (error) {
    console.error('Error fetching extreme event:', error);
    res.status(500).json({ message: error.message });
  }
};

/**
 * @desc    Acknowledge an extreme event
 * @route   PATCH /api/extreme-events/:id/acknowledge
 * @access  Public
 */
const acknowledgeExtremeEvent = async (req, res) => {
  try {
    const { acknowledgedBy } = req.body;

    const event = await ExtremeEvent.findById(req.params.id);
    if (!event) {
      return res.status(404).json({ message: 'Extreme event not found' });
    }

    event.acknowledged = true;
    event.acknowledgedBy = acknowledgedBy || 'Unknown';
    event.acknowledgedAt = new Date();

    const updatedEvent = await event.save();
    res.json(updatedEvent);
  } catch (error) {
    console.error('Error acknowledging extreme event:', error);
    res.status(500).json({ message: error.message });
  }
};

/**
 * @desc    Resolve an extreme event
 * @route   PATCH /api/extreme-events/:id/resolve
 * @access  Public
 */
const resolveExtremeEvent = async (req, res) => {
  try {
    const { notes } = req.body;

    const event = await ExtremeEvent.findById(req.params.id);
    if (!event) {
      return res.status(404).json({ message: 'Extreme event not found' });
    }

    event.resolved = true;
    event.resolvedAt = new Date();
    if (notes) {
      event.notes = notes;
    }

    const updatedEvent = await event.save();
    res.json(updatedEvent);
  } catch (error) {
    console.error('Error resolving extreme event:', error);
    res.status(500).json({ message: error.message });
  }
};

/**
 * @desc    Delete an extreme event
 * @route   DELETE /api/extreme-events/:id
 * @access  Public
 */
const deleteExtremeEvent = async (req, res) => {
  try {
    const event = await ExtremeEvent.findById(req.params.id);
    if (!event) {
      return res.status(404).json({ message: 'Extreme event not found' });
    }

    await event.deleteOne();
    res.json({ message: 'Extreme event deleted successfully' });
  } catch (error) {
    console.error('Error deleting extreme event:', error);
    res.status(500).json({ message: error.message });
  }
};

/**
 * @desc    Get extreme events statistics
 * @route   GET /api/extreme-events/stats
 * @access  Public
 */
const getExtremeEventsStats = async (req, res) => {
  try {
    const { storeId, store_id, days } = req.query;
    const finalStoreId = storeId || store_id;
    const daysBack = days ? parseInt(days) : 7;

    const filter = {};
    if (finalStoreId) {
      filter.storeId = finalStoreId;
    }

    // Last N days
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - daysBack);
    filter.timestamp = { $gte: startDate };

    const [total, bySeverity, acknowledged, resolved] = await Promise.all([
      ExtremeEvent.countDocuments(filter),
      ExtremeEvent.aggregate([
        { $match: filter },
        { $group: { _id: '$severity', count: { $sum: 1 } } },
      ]),
      ExtremeEvent.countDocuments({ ...filter, acknowledged: true }),
      ExtremeEvent.countDocuments({ ...filter, resolved: true }),
    ]);

    const severityMap = {};
    bySeverity.forEach((item) => {
      severityMap[item._id] = item.count;
    });

    res.json({
      period: `Last ${daysBack} days`,
      total,
      acknowledged,
      resolved,
      pending: total - resolved,
      bySeverity: severityMap,
    });
  } catch (error) {
    console.error('Error fetching extreme events stats:', error);
    res.status(500).json({ message: error.message });
  }
};

module.exports = {
  createExtremeEvent,
  getExtremeEvents,
  getExtremeEventById,
  acknowledgeExtremeEvent,
  resolveExtremeEvent,
  deleteExtremeEvent,
  getExtremeEventsStats,
  checkIfExtreme, // Export for use in sensorEventController
};
