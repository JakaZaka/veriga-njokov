"""
Flask API za Klasifikacijo Oblačil
===================================

Zagon:
python flask_clothing_api.py

API endpoint:
POST http://localhost:5001/api/classify

Uporaba v Node.js:
const FormData = require('form-data');
const form = new FormData();
form.append('image', fs.createReadStream('image.jpg'));
const response = await axios.post('http://localhost:5001/api/classify', form);
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import tensorflow as tf
import numpy as np
from PIL import Image
import io
import os

# Poskusi importati rembg
try:
    from rembg import remove
    REMBG_AVAILABLE = True
except ImportError:
    REMBG_AVAILABLE = False
    print("rembg ni nameščen - uporabljam fallback metodo")

app = Flask(__name__)
CORS(app)  # Enable CORS za Node.js backend

# Globalne spremenljivke
model = None
LABELS = ['T-shirt/top', 'Trouser', 'Pullover', 'Dress', 'Coat', 
          'Sandal', 'Shirt', 'Sneaker', 'Ankle boot']

# Mapping na tvoje MongoDB kategorije
CATEGORY_MAPPING = {
    'T-shirt/top': {'category': 'tops', 'subCategory': 't-shirt'},
    'Shirt': {'category': 'tops', 'subCategory': 'shirt'},
    'Pullover': {'category': 'tops', 'subCategory': 'pullover'},
    'Dress': {'category': 'dresses', 'subCategory': 'dress'},
    'Coat': {'category': 'outerwear', 'subCategory': 'coat'},
    'Trouser': {'category': 'bottoms', 'subCategory': 'trousers'},
    'Sandal': {'category': 'shoes', 'subCategory': 'sandals'},
    'Sneaker': {'category': 'shoes', 'subCategory': 'sneakers'},
    'Ankle boot': {'category': 'shoes', 'subCategory': 'boots'},
}

COLOR_NAMES = {
    'rdeča': (255, 0, 0),
    'temno rdeča': (139, 0, 0),
    'bordo': (128, 0, 32),
    'modra': (0, 100, 255),
    'svetlo modra': (135, 206, 250),
    'temno modra': (0, 0, 139),
    'navy': (0, 0, 128),
    'turkizna': (64, 224, 208),
    'zelena': (0, 200, 0),
    'svetlo zelena': (144, 238, 144),
    'temno zelena': (0, 100, 0),
    'rumena': (255, 255, 0),
    'oranžna': (255, 165, 0),
    'vijolična': (128, 0, 128),
    'rožnata': (255, 192, 203),
    'rjava': (139, 69, 19),
    'svetlo siva': (211, 211, 211),
    'siva': (128, 128, 128),
    'temno siva': (64, 64, 64),
    'črna': (30, 30, 30),
    'bela': (240, 240, 240),
    'bež': (245, 245, 220),
}

# Mapping slovenskih barv na angleške
COLOR_NAME_EN = {
    'črna': 'black',
    'bela': 'white',
    'siva': 'gray',
    'temno siva': 'dark gray',
    'svetlo siva': 'light gray',
    'rdeča': 'red',
    'temno rdeča': 'dark red',
    'bordo': 'burgundy',
    'modra': 'blue',
    'svetlo modra': 'light blue',
    'temno modra': 'dark blue',
    'navy': 'navy',
    'zelena': 'green',
    'svetlo zelena': 'light green',
    'temno zelena': 'dark green',
    'rumena': 'yellow',
    'oranžna': 'orange',
    'vijolična': 'purple',
    'rožnata': 'pink',
    'rjava': 'brown',
    'turkizna': 'turquoise',
    'bež': 'beige',
}


def load_model():
    """Naloži model ob zagonu"""
    global model
    model_path = os.environ.get('MODEL_PATH', 'saved_model_clothes_classification.keras')
    print(f"Nalaganje modela: {model_path}")
    model = tf.keras.models.load_model(model_path)
    print("Model naložen!")


def remove_background_rembg(image):
    """Odstrani ozadje z rembg"""
    if REMBG_AVAILABLE:
        return remove(image)
    else:
        return image  # Vrni originalno če rembg ni na voljo


def get_dominant_colors_simple(image, num_colors=3):
    """
    Poenostavljena detekcija barv (brez sklearn)
    Dobra za produkcijo
    """
    img_array = np.array(image)
    
    # Obravnava RGBA
    if len(img_array.shape) == 3 and img_array.shape[2] == 4:
        alpha = img_array[:, :, 3]
        mask = alpha > 50
        pixels = img_array[mask][:, :3]
    else:
        if len(img_array.shape) == 2:
            img_array = np.stack([img_array]*3, axis=-1)
        pixels = img_array.reshape(-1, 3)
        brightness = np.sum(pixels, axis=1)
        mask = brightness < 700
        pixels = pixels[mask]
    
    if len(pixels) == 0:
        return [{"name": "unknown", "name_sl": "neznana", "rgb": [128, 128, 128], "percentage": 100.0}]
    
    # Kvantizacija barv
    pixels_reduced = (pixels // 32) * 32
    
    # Preštej barve
    from collections import Counter
    pixel_tuples = [tuple(int(p) for p in pixel) for pixel in pixels_reduced]
    color_counts = Counter(pixel_tuples)
    
    most_common = color_counts.most_common(num_colors)
    total = sum(color_counts.values())
    
    results = []
    for color_rgb, count in most_common:
        color_name_sl = find_closest_color_name(color_rgb)
        color_name_en = COLOR_NAME_EN.get(color_name_sl, color_name_sl)
        percentage = (count / total) * 100
        results.append({
            "name": color_name_en,
            "name_sl": color_name_sl,
            "rgb": list(color_rgb),
            "percentage": round(percentage, 1)
        })
    
    return results


def find_closest_color_name(rgb):
    """Najde najbližje ime barve"""
    min_distance = float('inf')
    closest_name = "neznana"
    
    for name, color_rgb in COLOR_NAMES.items():
        distance = sum((a - b) ** 2 for a, b in zip(rgb, color_rgb)) ** 0.5
        if distance < min_distance:
            min_distance = distance
            closest_name = name
    
    return closest_name


def preprocess_for_classification(image):
    """
    Predobdelava za model
    """
    from PIL import ImageOps
    
    # Če je RGBA
    if image.mode == 'RGBA':
        bg = Image.new('RGB', image.size, (255, 255, 255))
        bg.paste(image, mask=image.split()[3])
        image = bg
    
    img_gray = image.convert('L')
    
    # PAD strategija
    width, height = img_gray.size
    if width > height:
        padding = ((width - height) // 2, 0)
        img_padded = ImageOps.expand(img_gray, border=padding, fill=255)
    else:
        padding = (0, (height - width) // 2)
        img_padded = ImageOps.expand(img_gray, border=padding, fill=255)
    
    img_resized = img_padded.resize((28, 28), Image.Resampling.LANCZOS)
    img_array = np.array(img_resized)
    img_inverted = 255 - img_array
    img_normalized = img_inverted / 255.0
    
    return img_normalized


@app.route('/api/classify', methods=['POST'])
def classify_clothing():
    """
    API endpoint za klasifikacijo oblačila
    
    Request:
        - image: file (multipart/form-data)
    
    Response:
        {
            "success": true,
            "data": {
                "classification": "T-shirt/top",
                "category": "tops",
                "subCategory": "t-shirt",
                "confidence": 0.956,
                "colors": [
                    {"name": "black", "name_sl": "črna", "rgb": [30, 30, 30], "percentage": 68.5},
                    {"name": "gray", "name_sl": "siva", "rgb": [128, 128, 128], "percentage": 22.1}
                ],
                "top5": [
                    {"label": "T-shirt/top", "confidence": 0.956},
                    {"label": "Shirt", "confidence": 0.035}
                ]
            }
        }
    """
    try:
        # Preveri če je image v requestu
        if 'image' not in request.files:
            return jsonify({
                "success": False,
                "error": "No image file provided"
            }), 400
        
        file = request.files['image']
        
        if file.filename == '':
            return jsonify({
                "success": False,
                "error": "Empty filename"
            }), 400
        
        # Naloži sliko
        image_bytes = file.read()
        image = Image.open(io.BytesIO(image_bytes))
        
        # 1. Odstrani ozadje
        img_no_bg = remove_background_rembg(image)
        
        # 2. Prepoznaj barve
        colors = get_dominant_colors_simple(img_no_bg, num_colors=3)
        
        # 3. Klasifikacija
        img_processed = preprocess_for_classification(img_no_bg)
        img_batch = np.expand_dims(img_processed, axis=0)
        probs = model.predict(img_batch, verbose=0)[0]
        
        prediction_idx = int(np.argmax(probs))
        prediction_label = LABELS[prediction_idx]
        confidence = float(probs[prediction_idx])
        
        # Top 5
        top5_indices = np.argsort(probs)[-5:][::-1]
        top5 = [
            {
                "label": LABELS[i],
                "confidence": round(float(probs[i]), 3)
            }
            for i in top5_indices
        ]
        
        # Mapiranje na MongoDB kategorije
        category_info = CATEGORY_MAPPING.get(prediction_label, {
            'category': 'other',
            'subCategory': prediction_label.lower()
        })
        
        # Sestavi odgovor
        response_data = {
            "success": True,
            "data": {
                "classification": prediction_label,
                "category": category_info['category'],
                "subCategory": category_info['subCategory'],
                "confidence": round(confidence, 3),
                "colors": colors,
                "primaryColor": colors[0]['name'] if colors else "unknown",
                "top5": top5
            }
        }
        
        return jsonify(response_data), 200
        
    except Exception as e:
        return jsonify({
            "success": False,
            "error": str(e)
        }), 500


@app.route('/api/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        "success": True,
        "status": "running",
        "model_loaded": model is not None,
        "rembg_available": REMBG_AVAILABLE
    }), 200


@app.route('/api/categories', methods=['GET'])
def get_categories():
    """Vrne seznam vseh kategorij in mapiranje"""
    return jsonify({
        "success": True,
        "data": {
            "labels": LABELS,
            "mapping": CATEGORY_MAPPING,
            "colors": list(COLOR_NAME_EN.values())
        }
    }), 200


if __name__ == '__main__':
    print("="*60)
    print("FLASK CLOTHING CLASSIFICATION API")
    print("="*60)
    
    # Naloži model
    load_model()
    
    print("\nAPI Endpoints:")
    print("  POST   http://localhost:5001/api/classify")
    print("  GET    http://localhost:5001/api/health")
    print("  GET    http://localhost:5001/api/categories")
    print("\nServer ready!")
    print("="*60 + "\n")
    
    # Zagon serverja
    port = int(os.environ.get('PORT', 5001))

    app.run(host='0.0.0.0', port=5001)