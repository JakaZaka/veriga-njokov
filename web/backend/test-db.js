const mongoose = require('mongoose');
const connectDB = require('./config/db');
const User = require('./models/User');
require('dotenv').config();

// Connect to MongoDB
connectDB();

// Create a test user
const createTestUser = async () => {
  try {
    const user = await User.create({
      username: 'testuser',
      email: 'test@example.com',
      password: 'password123'
    });
    
    console.log('Test user created:', user);
    
    // Disconnect from MongoDB
    await mongoose.disconnect();
    console.log('MongoDB Disconnected');
    
  } catch (error) {
    console.error('Error creating test user:', error.message);
    await mongoose.disconnect();
  }
};

createTestUser();