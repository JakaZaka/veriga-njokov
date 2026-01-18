/**
 * Node.js Controller za Klasifikacijo Oblačil
 * ==============================================
 * 
 * Povezuje Android app -> Node.js backend -> Python Flask API
 * 
 * Routes:
 * POST /api/clothing/classify - Analizira sliko in vrne rezultate
 * POST /api/clothing - Ustvari clothing item (obstoječi endpoint + AI)
 */

const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');
const ClothingItem = require('../models/ClothingItem');

// Python Flask API URL
const PYTHON_API_URL = process.env.PYTHON_API_URL || 'http://localhost:5001';

/**
 * Klasificira sliko oblačila z Python modelom
 * 
 * POST /api/clothing/classify
 * Body: multipart/form-data z 'image' file
 * 
 * Response:
 * {
 *   success: true,
 *   data: {
 *     classification: "T-shirt/top",
 *     category: "tops",
 *     subCategory: "t-shirt",
 *     confidence: 0.956,
 *     colors: [...],
 *     primaryColor: "black"
 *   }
 * }
 */
const classifyClothing = async (req, res) => {
  try {
    // Preveri če je image v requestu
    if (!req.file) {
      return res.status(400).json({
        success: false,
        message: 'No image file provided'
      });
    }

    // Ustvari FormData za Python API
    const form = new FormData();
    form.append('image', fs.createReadStream(req.file.path));

    // Pokliči Python API
    const pythonResponse = await axios.post(
      `${PYTHON_API_URL}/api/classify`,
      form,
      {
        headers: form.getHeaders(),
        timeout: 30000 // 30 sekund timeout
      }
    );

    // Vrni rezultate
    res.status(200).json(pythonResponse.data);

  } catch (error) {
    console.error('Error calling Python API:', error.message);
    
    if (error.response) {
      // Python API je vrnil napako
      return res.status(error.response.status).json({
        success: false,
        message: 'Classification failed',
        error: error.response.data
      });
    } else if (error.code === 'ECONNREFUSED') {
      // Python API ni dosegljiv
      return res.status(503).json({
        success: false,
        message: 'Classification service unavailable. Is Python API running?'
      });
    } else {
      // Druga napaka
      return res.status(500).json({
        success: false,
        message: 'Internal server error',
        error: error.message
      });
    }
  }
};

/**
 * Ustvari clothing item Z AI klasifikacijo
 * 
 * POST /api/clothing/create-with-ai
 * Body: multipart/form-data
 *   - image: file (obvezno)
 *   - name: string (opcijsko - auto-generirano če manjka)
 *   - Ostali parametri opcijsko (prepišejo AI rezultate)
 * 
 * Flow:
 * 1. Upload slike
 * 2. Pokliči Python API za klasifikacijo
 * 3. Uporabi AI rezultate kot default vrednosti
 * 4. Omogoči uporabniku da prepiše katerikoli parameter
 * 5. Shrani v MongoDB
 */
const createClothingItemWithAI = async (req, res) => {
  try {
    // Preveri če je image
    if (!req.file) {
      return res.status(400).json({
        success: false,
        message: 'Image is required'
      });
    }

    // 1. Pokliči Python API za klasifikacijo
    let aiResults = null;
    try {
      const form = new FormData();
      form.append('image', fs.createReadStream(req.file.path));

      const pythonResponse = await axios.post(
        `${PYTHON_API_URL}/api/classify`,
        form,
        {
          headers: form.getHeaders(),
          timeout: 30000
        }
      );

      aiResults = pythonResponse.data.data;
    } catch (aiError) {
      console.error('AI classification failed:', aiError.message);
      // Nadaljuj brez AI rezultatov
    }

    // 2. Pripravi podatke za clothing item
    // Uporabi AI rezultate kot default, omogoči override iz req.body
    const clothingData = {
      name: req.body.name || (aiResults ? 
        `${aiResults.primaryColor} ${aiResults.classification}` : 
        'Untitled'),
      
      category: req.body.category || (aiResults?.category || 'other'),
      
      subCategory: req.body.subCategory || (aiResults?.subCategory || ''),
      
      color: req.body.color || (aiResults?.primaryColor || ''),
      
      season: req.body.season || ['all'],
      
      size: req.body.size || '',
      
      imageUrl: "/images/" + req.file.filename,
      
      notes: req.body.notes || '',
      
      user: req.user?._id || req.session.userId,
      
      // Shrani AI rezultate v metadata
      metadata: {
        aiClassification: aiResults ? {
          classification: aiResults.classification,
          confidence: aiResults.confidence,
          colors: aiResults.colors,
          top5: aiResults.top5,
          timestamp: new Date()
        } : null,
        userOverride: {
          category: req.body.category !== undefined,
          subCategory: req.body.subCategory !== undefined,
          color: req.body.color !== undefined
        }
      }
    };

    // 3. Ustvari clothing item
    const clothingItem = new ClothingItem(clothingData);
    const savedItem = await clothingItem.save();

    // 4. Vrni rezultat
    res.status(201).json({
      success: true,
      data: {
        clothingItem: savedItem,
        aiResults: aiResults // Vrni tudi AI rezultate za prikaz v UI
      }
    });

  } catch (error) {
    console.error('Error creating clothing item:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to create clothing item',
      error: error.message
    });
  }
};

/**
 * Posodobi clothing item
 * (Obstoječi endpoint - lahko uporabljaš tega za user corrections)
 */
const updateClothingItem = async (req, res) => {
  try {
    const { id } = req.params;
    
    const clothingItem = await ClothingItem.findById(id);
    
    if (!clothingItem) {
      return res.status(404).json({
        success: false,
        message: 'Clothing item not found'
      });
    }

    // Preveri lastništvo
    if (clothingItem.user.toString() !== (req.user?._id || req.session.userId).toString()) {
      return res.status(403).json({
        success: false,
        message: 'Not authorized'
      });
    }

    // Posodobi polja
    if (req.body.name) clothingItem.name = req.body.name;
    if (req.body.category) clothingItem.category = req.body.category;
    if (req.body.subCategory) clothingItem.subCategory = req.body.subCategory;
    if (req.body.color) clothingItem.color = req.body.color;
    if (req.body.season) clothingItem.season = req.body.season;
    if (req.body.size) clothingItem.size = req.body.size;
    if (req.body.notes) clothingItem.notes = req.body.notes;

    // Označi da je uporabnik ročno posodobil
    if (clothingItem.metadata) {
      clothingItem.metadata.userOverride = {
        ...clothingItem.metadata.userOverride,
        manuallyEdited: true,
        lastEditDate: new Date()
      };
    }

    const updatedItem = await clothingItem.save();

    res.status(200).json({
      success: true,
      data: updatedItem
    });

  } catch (error) {
    console.error('Error updating clothing item:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to update clothing item',
      error: error.message
    });
  }
};

/**
 * Health check za Python API
 */
const checkPythonAPIHealth = async (req, res) => {
  try {
    const response = await axios.get(`${PYTHON_API_URL}/api/health`, {
      timeout: 5000
    });
    
    res.status(200).json({
      success: true,
      pythonAPI: response.data
    });
  } catch (error) {
    res.status(503).json({
      success: false,
      message: 'Python API is not available',
      error: error.message
    });
  }
};

module.exports = {
  classifyClothing,
  createClothingItemWithAI,
  updateClothingItem,
  checkPythonAPIHealth
};