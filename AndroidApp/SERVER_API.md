# Server API Documentation

## Opis

Ta dokument opisuje API končne točke (endpoints), ki jih aplikacija Closy uporablja za pošiljanje podatkov senzorjev na strežnik.

## Endpoints

### 1. Pošiljanje posameznega podatka

**URL:** `POST /api/sensor-data`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "timestamp": 1704326400000,
  "sensor_type": "ACCELEROMETER",
  "location": {
    "latitude": 46.5547,
    "longitude": 15.6466,
    "altitude": 275.0,
    "accuracy": 10.5
  },
  "data": {
    "x": 0.123,
    "y": 9.81,
    "z": 0.456
  }
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Data received successfully",
  "id": "generated-id-12345"
}
```

### 2. Pošiljanje več podatkov naenkrat (Batch)

**URL:** `POST /api/sensor-data/batch`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
[
  {
    "timestamp": 1704326400000,
    "sensor_type": "ACCELEROMETER",
    "location": {...},
    "data": {...}
  },
  {
    "timestamp": 1704326401000,
    "sensor_type": "GYROSCOPE",
    "location": {...},
    "data": {...}
  }
]
```

**Response:**
```json
{
  "status": "success",
  "message": "Batch data received successfully",
  "count": 2,
  "ids": ["id1", "id2"]
}
```

## Tipi senzorjev in podatkovne strukture

### ACCELEROMETER
```json
{
  "sensor_type": "ACCELEROMETER",
  "data": {
    "x": 0.123,  // m/s² 
    "y": 9.81,   // m/s²
    "z": 0.456   // m/s²
  }
}
```

### GYROSCOPE
```json
{
  "sensor_type": "GYROSCOPE",
  "data": {
    "x": 0.01,  // rad/s
    "y": 0.02,  // rad/s
    "z": 0.03   // rad/s
  }
}
```

### LIGHT
```json
{
  "sensor_type": "LIGHT",
  "data": {
    "lux": 250.5  // lux
  }
}
```

### PROXIMITY
```json
{
  "sensor_type": "PROXIMITY",
  "data": {
    "distance": 5.0  // cm
  }
}
```

### TEMPERATURE
```json
{
  "sensor_type": "TEMPERATURE",
  "data": {
    "celsius": 22.5  // °C
  }
}
```

### GPS
```json
{
  "sensor_type": "GPS",
  "location": {
    "latitude": 46.5547,
    "longitude": 15.6466,
    "altitude": 275.0,
    "accuracy": 10.5
  },
  "data": {}
}
```

### CAMERA
```json
{
  "sensor_type": "CAMERA",
  "data": {
    "imagePath": "/storage/emulated/0/Android/data/com.example.closy/files/photos/photo_1704326400000.jpg",
    "width": 1920,
    "height": 1080
  }
}
```

### MICROPHONE
```json
{
  "sensor_type": "MICROPHONE",
  "data": {
    "amplitude": 1234.56,
    "filePath": "/storage/emulated/0/Android/data/com.example.closy/files/audio/audio_1704326400000.3gp"
  }
}
```

## Primer implementacije strežnika (Node.js + Express)

```javascript
const express = require('express');
const app = express();

app.use(express.json());

// Endpoint za posamezen podatek
app.post('/api/sensor-data', (req, res) => {
  const sensorData = req.body;
  
  console.log('Received sensor data:', sensorData);
  
  // Shrani v bazo podatkov
  // saveToDB(sensorData);
  
  res.json({
    status: 'success',
    message: 'Data received successfully',
    id: generateId()
  });
});

// Endpoint za batch podatke
app.post('/api/sensor-data/batch', (req, res) => {
  const sensorDataArray = req.body;
  
  console.log('Received batch data:', sensorDataArray.length, 'items');
  
  // Shrani vse v bazo podatkov
  // sensorDataArray.forEach(data => saveToDB(data));
  
  res.json({
    status: 'success',
    message: 'Batch data received successfully',
    count: sensorDataArray.length
  });
});

app.listen(3000, () => {
  console.log('Server running on port 3000');
});
```

## Primer implementacije strežnika (Python + Flask)

```python
from flask import Flask, request, jsonify
import json
from datetime import datetime

app = Flask(__name__)

@app.route('/api/sensor-data', methods=['POST'])
def receive_sensor_data():
    sensor_data = request.get_json()
    
    print(f"Received sensor data: {sensor_data}")
    
    # Shrani v bazo podatkov
    # save_to_db(sensor_data)
    
    return jsonify({
        'status': 'success',
        'message': 'Data received successfully',
        'id': generate_id()
    })

@app.route('/api/sensor-data/batch', methods=['POST'])
def receive_batch_data():
    sensor_data_array = request.get_json()
    
    print(f"Received batch data: {len(sensor_data_array)} items")
    
    # Shrani vse v bazo podatkov
    # for data in sensor_data_array:
    #     save_to_db(data)
    
    return jsonify({
        'status': 'success',
        'message': 'Batch data received successfully',
        'count': len(sensor_data_array)
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=3000)
```

## MQTT Alternativa

Za uporabo MQTT namesto HTTP:

### Konfiguracija MQTT:
- **Broker URL:** `tcp://broker.hivemq.com:1883`
- **Topic za objavljanje:** `closy/sensor/{sensor_type}`
- **QoS:** 1 (At least once delivery)
- **Retained:** false

### Primer MQTT Payload:
```json
{
  "device_id": "android-device-123",
  "timestamp": 1704326400000,
  "sensor_type": "ACCELEROMETER",
  "location": {...},
  "data": {...}
}
```

## Nasveti za implementacijo strežnika

1. **Validacija podatkov:** Vedno validirajte prejetje podatke
2. **Avtentikacija:** Dodajte API ključe ali OAuth2 za varnost
3. **Rate limiting:** Omejite število zahtevkov na minuto
4. **Baza podatkov:** Uporabite MongoDB, PostgreSQL ali InfluxDB za shranjevanje časovnih vrst
5. **Skalabilnost:** Uporabite message queue (RabbitMQ, Kafka) za veliko količino podatkov
6. **Monitoring:** Implementirajte logiranje in monitoring (Grafana, Prometheus)

## Testiranje

### cURL primer:
```bash
curl -X POST http://your-server.com/api/sensor-data \
  -H "Content-Type: application/json" \
  -d '{
    "timestamp": 1704326400000,
    "sensor_type": "ACCELEROMETER",
    "location": {
      "latitude": 46.5547,
      "longitude": 15.6466,
      "altitude": 275.0,
      "accuracy": 10.5
    },
    "data": {
      "x": 0.123,
      "y": 9.81,
      "z": 0.456
    }
  }'
```

### Postman Collection
Uvozite priloženo Postman kolekcijo za enostavno testiranje API-ja.

