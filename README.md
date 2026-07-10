# Veriga Njokov

A multi-module digital twin project for fashion retail monitoring, combining mobile sensing, geospatial visualization, distributed computing, and computer vision.

## Overview

Veriga Njokov is a cross-domain software project that models operational conditions in fashion stores across a city.  
It brings together:

- an Android client for event and sensor data collection,
- a map-based visualization layer for store and user status,
- an MPI-based blockchain mining prototype for distributed event handling experiments,
- and computer vision prototypes for people counting and clothing-type recognition.

The repository demonstrates practical integration of mobile, web, data, and ML/distributed components in one system.

## Implemented Features

### 1) Android data collection app
- Captures sensor-driven and user-triggered events.
- Sends data toward backend/server-side components.
- Includes configurable application settings (notifications, language, theme).

### 2) Interactive 2D map and detail view
- Displays store/user locations on a 2D map.
- Loads data through API calls.
- Supports detailed information panels on location selection.

### 3) MPI-parallel blockchain prototype
- Implements a block mining algorithm with MPI parallelization.
- Supports inter-process block communication.
- Includes integration scripting for exceptional event ingestion.

### 4) Sensor simulation flow
- Simulates in-store people-count style input data.
- Persists generated data at intervals through backend/web components.

### 5) Computer vision prototypes
- People counting on images using YOLOv8n.
- Initial work on clothing-type recognition.

## Project Snapshots

### Android application
<img width="490" height="1096" alt="Android app screen 1" src="https://github.com/user-attachments/assets/e2594098-a912-4b1a-b8d1-e358849cb4d4" />
<img width="495" height="1106" alt="Android app screen 2" src="https://github.com/user-attachments/assets/49ebe755-31ec-4891-8ce4-a7fb43308a23" />
<img width="495" height="1106" alt="Android app screen 3" src="https://github.com/user-attachments/assets/c76df0b8-bfc5-451d-96ea-75613512db0c" />
<img width="466" height="1109" alt="Android app screen 4" src="https://github.com/user-attachments/assets/5d1893b6-897b-4b5f-90d9-687d502b6453" />
<img width="495" height="572" alt="Android app screen 5" src="https://github.com/user-attachments/assets/8ba9112a-8d66-4ba8-8ec4-c423f7dcedaf" />

### Map visualization
<img width="1919" height="1035" alt="Map locations view" src="https://github.com/user-attachments/assets/3aac9e4d-3cb2-4d27-b77a-24d69992cd2a" />
<img width="1918" height="1032" alt="Map details view 1" src="https://github.com/user-attachments/assets/4711a87f-2239-40ca-8066-867a539535f9" />
<img width="1894" height="1024" alt="Map details view 2" src="https://github.com/user-attachments/assets/e64fcdb1-1926-412d-8554-312d50271eba" />

### Settings and simulation UI
<img width="381" height="638" alt="Settings screen" src="https://github.com/user-attachments/assets/66ed357a-d5f1-4e2b-82a2-16f531d46223" />
<img width="373" height="742" alt="Sensor simulation screen" src="https://github.com/user-attachments/assets/9e955a7b-f812-4a19-a3e4-864f94d38b00" />

### Blockchain + CV outputs
![MPI mining screenshot 1](images/screenshot1.png)
![MPI mining screenshot 2](images/screenshot2.png)
![PORA integration screenshot 1](images/screenshot3.png)
![PORA integration screenshot 2](images/screenshot4.png)
![CV people counting sample](images/primer_1_annotated.jpg)
![CV clothing recognition baseline](images/screenshot5.png)

## Technology Stack

Based on repository language composition and implemented modules:

- **Kotlin** (Android/mobile)
- **JavaScript / HTML / CSS** (web/map UI)
- **Python** (MPI prototype scripts, CV/ML)
- **Java** (supporting components)

Language share:
- Kotlin 39.1%
- JavaScript 31.6%
- HTML 11.7%
- Python 8.2%
- Java 7.5%
- CSS 1.9%

## Repository

- **GitHub:** https://github.com/JakaZaka/veriga-njokov
- **Default branch:** `develop`

## Roadmap (Planned Improvements)

- Real-time end-to-end event flow between mobile, backend, and map.
- Time-based store occupancy simulation and prediction views.
- Extended blockchain performance/acceleration analysis across thread/process counts.
- Improved clothing recognition robustness on broader user-captured images.
- Enhanced map rendering performance via local tile caching.
- Event-location selection and richer store data management workflows.

## Team

- Jaka Počkaj
- Anđelija Lazarević
- Nela Copot
