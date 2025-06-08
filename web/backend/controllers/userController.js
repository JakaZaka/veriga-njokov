const User = require('../models/User');
const Outfit = require('../models/Outfit');
const ClothingItem = require('../models/ClothingItem');
const jwt = require('jsonwebtoken');
const NodeGeocoder = require('node-geocoder');
const geocoder = NodeGeocoder({ provider: 'openstreetmap' });
const path = require('path');
const fs = require('fs');
const turf = require('@turf/turf');



// Generate JWT token
const generateToken = (id) => {
  return jwt.sign({ id }, process.env.JWT_SECRET, {
    expiresIn: '30d',
  });
};

// @desc    Register a new user
// @route   POST /api/users
// @access  Public
const registerUser = async (req, res) => {
  try {
    const { username, email, password, contactInfo } = req.body;

    const userExists = await User.findOne({ email });
    if (userExists) {
      res.status(400);
      throw new Error('User already exists');
    }

    const user = await User.create({
      username,
      email,
      password,
      contactInfo // <-- add this line
    });

    if (user) {
      res.status(201).json({
        _id: user._id,
        username: user.username,
        email: user.email,
        role: user.role,
        token: generateToken(user._id),
      });
    } else {
      res.status(400);
      throw new Error('Invalid user data');
    }
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};

// @desc    Auth user & get token
// @route   POST /api/users/login
// @access  Public
const loginUser = async (req, res) => {
  try {
    const { username, password } = req.body;

    // Find user by username only
    const user = await User.findOne({ username });

    if (user && (await user.matchPassword(password))) {
      req.session.userId = user._id; // Store user ID in session
      res.json({
        _id: user._id,
        username: user.username,
        email: user.email,
        role: user.role,
        location: user.location,
        token: generateToken(user._id),
      });
    } else {
      res.status(401);
      throw new Error('Invalid username or password');
    }
  } catch (error) {
    res.status(401).json({ message: error.message });
  }
};
// @desc    Get user profile
// @route   GET /api/users/profile
// @access  Private
const getUserProfile = async (req, res) => {
  try {
    const user = await User.findById(req.user._id).select('-password');
    if (user) {
      res.json({
        _id: user._id,
        username: user.username,
        email: user.email,
        role: user.role,
        avatar: user.avatar,
        contactInfo: user.contactInfo, // <-- add this line
      });
    } else {
      res.status(404).json({ message: 'User not found' });
    }
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Update user profile
// @route   PUT /api/users/profile
// @access  Private
const updateUserProfile = async (req, res) => {
  try {
    const user = await User.findById(req.user._id);

    if (user) {
      user.username = req.body.username || user.username;
      user.email = req.body.email || user.email;
      if (req.body.password) {
        user.password = req.body.password;
      }
      if (req.body.avatar) {
        user.avatar = req.body.avatar;
      }
      // Update contactInfo if present
      if (req.body.contactInfo) {
        user.contactInfo = {
          ...user.contactInfo,
          ...req.body.contactInfo
        };
      }
      // Handle location/address
      if (req.body.address) {
        // Geocode the address to get coordinates
        const geoRes = await geocoder.geocode(req.body.address);
        if (geoRes && geoRes.length > 0) {
          const geo = geoRes[0];
          user.location = {
            address: req.body.address,
            city: geo.city || "",
            country: geo.country || "",
            coordinates: {
              type: "Point",
              coordinates: [geo.longitude, geo.latitude]
            }
          };
        } else {
          // If geocoding fails, just save the address
          user.location = {
            address: req.body.address
          };
        }
      }
      const updatedUser = await user.save();
      res.json({
        _id: updatedUser._id,
        username: updatedUser.username,
        email: updatedUser.email,
        role: updatedUser.role,
        avatar: updatedUser.avatar,
        contactInfo: updatedUser.contactInfo,
        location: updatedUser.location, // <-- add this
      });
    } else {
      res.status(404).json({ message: 'User not found' });
    }
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Delete user
// @route   DELETE /api/users
// @access  Private
const deleteUser = async (req, res) => {
  try {
    const user = await User.findById(req.user._id);
    if (user) {
      await user.remove();
      res.json({ message: 'User removed' });
    } else {
      res.status(404).json({ message: 'User not found' });
    }
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Get all users
// @route   GET /api/users
// @access  Public
const getAllUsers = async (req, res) => {
  try {
    const users = await User.find({}).select('-password');
    // Wrap response in the expected format
    res.json({
      success: true,
      data: users,
      message: 'Users retrieved successfully'
    });
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      error: error.message 
    });
  }
};

// @desc    Delete user (admin only)
// @route   DELETE /api/users/:id
// @access  Private/Admin
const deleteUserById = async (req, res) => {
  try {
    const user = await User.findById(req.params.id);
    
    if (!user) {
      return res.status(404).json({ message: 'User not found' });
    }
    
    if (user.role === 'admin' && req.user._id.toString() === user._id.toString()) {
      return res.status(400).json({ message: 'Cannot delete your own admin account' });
    }
    
    await User.findByIdAndDelete(req.params.id);
    res.json({ message: 'User deleted successfully' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// @desc    Update user role (admin only)
// @route   PUT /api/users/:id/role
// @access  Private/Admin
const updateUserRole = async (req, res) => {
  try {
    const { role } = req.body;
    
    if (!['user', 'admin'].includes(role)) {
      return res.status(400).json({ message: 'Invalid role' });
    }
    
    const user = await User.findById(req.params.id);
    
    if (!user) {
      return res.status(404).json({ message: 'User not found' });
    }
    
    user.role = role;
    await user.save();
    
    res.json({ message: 'User role updated successfully', user: { ...user.toObject(), password: undefined } });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};


const nearbyUsers = async (req, res) => {
  try {
    const { longitude, latitude, maxDistance} = req.query; // maxDistance in meters, default 10km
    
    if (!longitude || !latitude) {
      return res.status(400).json({ message: 'Longitude and latitude are required' });
    }
    console.log(`Searching near [${longitude}, ${latitude}] within ${maxDistance}m`);

    
    // Find stores within the specified radius
    const users = await User.find({
      'location.coordinates': {
        $near: {
          $geometry: {
            type: 'Point',
            coordinates: [parseFloat(longitude), parseFloat(latitude)],
          },
          $maxDistance: parseInt(maxDistance),
        },
      },
    });
    
   const usersWithExtras = await Promise.all(
      users.map(async user => {
        const outfits = await Outfit.find({ user: user._id }).populate('items.item');
        const clothesForSale = await ClothingItem.find({ user: user._id, wantToGive: true });
        console.log(clothesForSale);

        return {
          ...user.toObject(),
          outfits,
          clothesForSale,
        };
      })
    );

    res.json(usersWithExtras);
  } catch (err) {
    console.error('Error fetching nearby users:', err);
    res.status(500).json({ error: 'Failed to fetch users' });
  }
};


const getSalesPerDistrict = async (req, res) => {
  //for sorting the users into districts in maribor
  const districtsGeoJSONPath = path.join(__dirname, '../public/geo/maribor_districts.json');
  const districtsGeoJSON = JSON.parse(fs.readFileSync(districtsGeoJSONPath));

  try {
    const users = await User.find();
    const items = await ClothingItem.find({ wantToGive: true });

    // Create district lookup
    const districtCounts = {};

    for (const feature of districtsGeoJSON.features) {
      const name = feature.properties.name;
      districtCounts[name] = {
        tops: 0,
        bottoms: 0,
        shoes: 0,
        outerwear: 0,
        accessories: 0,
        other: 0,
      };
    }

    for (const user of users) {
      const coords = user.location?.coordinates?.coordinates;
      if (!coords || coords.length !== 2) continue;

      const userPoint = turf.point(coords);

      const district = districtsGeoJSON.features.find((feature) =>
        turf.booleanPointInPolygon(userPoint, feature.geometry)
      );

      if (!district) continue;
      const districtName = district.properties.name;

      // Find the user’s items they want to give
      const userItems = items.filter((item) => item.user.toString() === user._id.toString() && item.wantToGive);

      for (const item of userItems) {
        const cat = item.category;
        if (!districtCounts[districtName][cat]) {
          districtCounts[districtName][cat] = 1;
        } else {
          districtCounts[districtName][cat]++;
        }
      }
    }

    const response = Object.entries(districtCounts).map(([district, categories]) => ({
      district,
      ...categories,
    }));

    res.json(response);
  } catch (err) {
    console.error("Error in getSalesPerDistrict:", err);
    res.status(500).json({ message: "Server error while computing sales per district" });
  }
};

const getRequests = async (req, res) => {
  try {
    console.log("Session data:", req.session);
    const clothes = await ClothingItem.find({
      user: req.session.userId,
      wantToGive: true,
    }).populate('wantToGet', 'username email'); 


    const requests = clothes.map(item => ({
      itemId: item._id,
      name: item.name,
      category: item.category,
      imageUrl: item.imageUrl,
      wantToGet: item.wantToGet, 
    }));

    res.json(requests);
  } catch (error) {
    console.error("Error retrieving items with requests:", error);
    res.status(500).json({ message: error.message });
  }
};

module.exports = { 
  registerUser, 
  loginUser, 
  getUserProfile, 
  updateUserProfile, 
  deleteUser,
  getAllUsers,
  deleteUserById,
  updateUserRole,
  nearbyUsers,
  getSalesPerDistrict,
  getRequests
};