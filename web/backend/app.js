const express = require('express');
const connectDB = require('./config/db');
require('dotenv').config();
var path = require('path');

// Connect to MongoDB
connectDB();

const app = express();

var cors = require('cors');
var allowedOrigins = ['http://localhost:3000', 'http://localhost:3001'];
app.use(cors({
  credentials: true,
  origin: function(origin, callback){
    // Allow requests with no origin (mobile apps, curl)
    if(!origin) return callback(null, true);
    if(allowedOrigins.indexOf(origin)===-1){
      var msg = "The CORS policy does not allow access from the specified Origin.";
      return callback(new Error(msg), false);
    }
    return callback(null, true);
  }
}));

// Middleware
app.use(express.json());

// Basic route for testing
app.get('/', (req, res) => {
  res.send('Closy API is running');
});

app.use(express.static(path.join(__dirname, 'public')));

// routes
const userRoutes = require('./routes/userRoutes');
const clothingItemRoutes = require('./routes/clothingItemRoutes');
const outfitRoutes = require('./routes/outfitRoutes');
const weatherRoutes = require('./routes/weatherRoutes');
const clothingStoreRoutes = require('./routes/clothingStoreRoutes'); // Add this line
const enumRoutes = require('./routes/enumRoutes'); // Add this line
const locationRoutes = require('./routes/locationRoutes'); 

// API routes
app.use('/api/users', userRoutes);
app.use('/clothing', clothingItemRoutes);
app.use('/api/outfits', outfitRoutes);
app.use('/api/weather', weatherRoutes);

app.use('/stores', clothingStoreRoutes); 
app.use('/enums', enumRoutes); 
app.use('/locations', locationRoutes);
/* Start server
const PORT = process.env.PORT || 8000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});*/


module.exports = app;