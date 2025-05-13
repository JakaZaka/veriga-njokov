const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const userSchema = mongoose.Schema(
  {
    username: {
      type: String,
      required: true,
      unique: true,
    },
    email: {
      type: String,
      required: true,
      unique: true,
    },
    password: {
      type: String,
      required: true,
    },
    role: {
      type: String,
      enum: ['user', 'admin'],
      default: 'user',
    },
    // location fields
    location: {
      address: {
        type: String,
      },
      city: {
        type: String,
      },
      country: {
        type: String,
        default: 'Slovenia',
      },
      coordinates: {
        // GeoJSON format for MongoDB geospatial queries
        type: {
          type: String,
          enum: ['Point'],
          default: 'Point',
        },
        coordinates: {
          // [longitude, latitude]
          type: [Number],
          default: [15.6467, 46.5547], // Default coordinates for Maribor
        }
      },
    },
    // privacy settings related to wardrobe sharing
    privacySettings: {
      showOnMap: {
        type: Boolean,
        default: true,
      },
      publicWardrobe: {
        type: Boolean,
        default: false,
      },
      publicOutfits: {
        type: Boolean,
        default: false,
      },
    },
    preferences: {
      type: mongoose.Schema.Types.Mixed,
    },
  },
  { timestamps: true }
);

// geospatial index for efficient location-based queries
userSchema.index({ 'location.coordinates': '2dsphere' });

// Password encryption middleware
userSchema.pre('save', async function (next) {
  if (!this.isModified('password')) {
    next();
  }
  const salt = await bcrypt.genSalt(10);
  this.password = await bcrypt.hash(this.password, salt);
});

// Method to check if entered password matches encrypted password
userSchema.methods.matchPassword = async function (enteredPassword) {
  return await bcrypt.compare(enteredPassword, this.password);
};

const User = mongoose.model('User', userSchema);
module.exports = User;