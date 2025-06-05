const express = require('express');
const connectDB = require('./config/db');
require('dotenv').config();
var path = require('path');

// Connect to MongoDB
connectDB();

const app = express();

var cors = require('cors');
var allowedOrigins = ['http://localhost:3000', 'http://localhost:3001', 'http://frontend:3000', 'http://frontend:3001'];
app.use(cors({
  credentials: true,
  origin: true/*function(origin, callback){
    // Allow requests with no origin (mobile apps, curl)
    if(!origin) return callback(null, true);
    if(allowedOrigins.indexOf(origin)===-1){
      var msg = "The CORS policy does not allow access from the specified Origin. ${origin}";
      return callback(new Error(msg), false);
    }
    return callback(null, true);
  }*/
}));

//app.options('*', cors());

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


var session = require('express-session');
var MongoStore = require('connect-mongo');
app.use(session({
  secret: 'work hard',
  resave: true,
  saveUninitialized: false,
  store: MongoStore.create({mongoUrl: process.env.MONGO_URI})
}));
//Shranimo sejne spremenljivke v locals
//Tako lahko do njih dostopamo v vseh view-ih (glej layout.hbs)
app.use(function (req, res, next) {
  res.locals.session = req.session;
  next();
});


// API routes
app.use('/api/users', userRoutes);
app.use('/api/clothing', clothingItemRoutes);
app.use('/api/outfits', outfitRoutes);
app.use('/api/weather', weatherRoutes);

app.use('/api/stores', clothingStoreRoutes); 
app.use('/api/enums', enumRoutes); 
app.use('/api/locations', locationRoutes);
/* Start server
const PORT = process.env.PORT || 8000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});*/


module.exports = app;