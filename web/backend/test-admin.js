const User = require('./models/User');
const connectDB = require('./config/db');
require('dotenv').config();

connectDB();

async function createAdmin() {
  try {
    const adminUser = await User.findOne({ email: 'admin@test.com' });
    
    if (adminUser) {
      adminUser.role = 'admin';
      await adminUser.save();
      console.log('User updated to admin');
    } else {
      const newAdmin = new User({
        username: 'admin',
        email: 'admin@test.com',
        password: 'admin123', // bo hashiran v pre-save hook
        role: 'admin'
      });
      await newAdmin.save();
      console.log('Admin user created');
    }
    
    process.exit();
  } catch (error) {
    console.error(error);
    process.exit(1);
  }
}

createAdmin();