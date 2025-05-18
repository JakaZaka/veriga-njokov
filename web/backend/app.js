const express = require('express');
const connectDB = require('./config/db');
require('dotenv').config();
const cors = require('cors');

// Connect to MongoDB
connectDB();

const app = express();

// Middleware
app.use(cors({ origin: 'http://localhost:3000', credentials: true }));
app.use(express.json( {limit: '100mb'})); // Limit request size to 10MB

// Basic route for testing
app.get('/', (req, res) => {
  res.send('Closy API is running');
});

// routes
const userRoutes = require('./routes/userRoutes');
const clothingItemRoutes = require('./routes/clothingItemRoutes');
const outfitRoutes = require('./routes/outfitRoutes');
const weatherRoutes = require('./routes/weatherRoutes');
const clothingStoreRoutes = require('./routes/clothingStoreRoutes');

// API routes
app.use('/api/users', userRoutes);
app.use('/api/clothing', clothingItemRoutes);
app.use('/api/outfits', outfitRoutes);
app.use('/api/weather', weatherRoutes);
app.use('/api/stores', clothingStoreRoutes);

// Start server
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});