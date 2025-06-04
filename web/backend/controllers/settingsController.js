const Settings = require('../models/Settings');

// @desc    Get all settings
// @route   GET /api/admin/settings
// @access  Private/Admin
const getAllSettings = async (req, res) => {
  try {
    const settings = await Settings.find({});
    res.json(settings);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Get settings by category
// @route   GET /api/admin/settings/category/:category
// @access  Private/Admin
const getSettingsByCategory = async (req, res) => {
  try {
    const { category } = req.params;
    const settings = await Settings.find({ category });
    res.json(settings);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Update setting
// @route   PUT /api/admin/settings/:key
// @access  Private/Admin
const updateSetting = async (req, res) => {
  try {
    const { key } = req.params;
    const { value } = req.body;
    
    const setting = await Settings.findOne({ key });
    
    if (!setting) {
      return res.status(404).json({ message: 'Setting not found' });
    }
    
    if (!setting.editable) {
      return res.status(400).json({ message: 'This setting is not editable' });
    }
    
    setting.value = value;
    await setting.save();
    
    res.json(setting);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Create new setting
// @route   POST /api/admin/settings
// @access  Private/Admin
const createSetting = async (req, res) => {
  try {
    const { key, value, description, category, dataType } = req.body;
    
    const existingSetting = await Settings.findOne({ key });
    if (existingSetting) {
      return res.status(400).json({ message: 'Setting with this key already exists' });
    }
    
    const setting = new Settings({
      key,
      value,
      description,
      category,
      dataType
    });
    
    await setting.save();
    res.status(201).json(setting);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Delete setting
// @route   DELETE /api/admin/settings/:key
// @access  Private/Admin
const deleteSetting = async (req, res) => {
  try {
    const { key } = req.params;
    
    const setting = await Settings.findOne({ key });
    
    if (!setting) {
      return res.status(404).json({ message: 'Setting not found' });
    }
    
    if (!setting.editable) {
      return res.status(400).json({ message: 'This setting cannot be deleted' });
    }
    
    await Settings.findOneAndDelete({ key });
    res.json({ message: 'Setting deleted successfully' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Initialize default settings
// @route   POST /api/admin/settings/init
// @access  Private/Admin
const initializeSettings = async (req, res) => {
  try {
    const defaultSettings = [
      {
        key: 'weather_api_key',
        value: '',
        description: 'API key for weather service',
        category: 'weather',
        dataType: 'string'
      },
      {
        key: 'weather_update_interval',
        value: 3600000,
        description: 'Weather update interval in milliseconds',
        category: 'weather',
        dataType: 'number'
      },
      {
        key: 'default_location',
        value: 'Ljubljana',
        description: 'Default location for weather data',
        category: 'weather',
        dataType: 'string'
      },
      {
        key: 'max_upload_size',
        value: 10485760,
        description: 'Maximum file upload size in bytes',
        category: 'general',
        dataType: 'number'
      },
      {
        key: 'enable_notifications',
        value: true,
        description: 'Enable system notifications',
        category: 'notifications',
        dataType: 'boolean'
      }
    ];

    for (const settingData of defaultSettings) {
      const existing = await Settings.findOne({ key: settingData.key });
      if (!existing) {
        await Settings.create(settingData);
      }
    }

    res.json({ message: 'Default settings initialized' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

module.exports = {
  getAllSettings,
  getSettingsByCategory,
  updateSetting,
  createSetting,
  deleteSetting,
  initializeSettings
};