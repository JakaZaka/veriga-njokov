package com.example.closy

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.closy.sensors.LocationProvider
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.view.View
import android.widget.TextView
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.util.Rational
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import java.io.FileOutputStream

/**
 * Camera Activity - Allows users to take photos
 */
class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var captureButton: MaterialButton
    private lateinit var statusText: TextView
    private lateinit var photoPreviewCard: View
    private lateinit var photoPathText: TextView
    private lateinit var periodicButton: MaterialButton
    private lateinit var intervalSpinner: Spinner

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var periodicJob: Job? = null
    private var periodicIntervalSeconds = 10 // default interval
    private lateinit var locationProvider: LocationProvider
    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Camera"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize views
        previewView = findViewById(R.id.previewView)
        captureButton = findViewById(R.id.captureButton)
        statusText = findViewById(R.id.statusText)
        photoPreviewCard = findViewById(R.id.photoPreviewCard)
        photoPathText = findViewById(R.id.photoPathText)
        periodicButton = findViewById(R.id.periodicButton)
        intervalSpinner = findViewById(R.id.intervalSpinner)

        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Check camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Setup capture button
        captureButton.setOnClickListener {
            takePhoto()
        }

        // Setup interval spinner
        setupIntervalSpinner(intervalSpinner)
        intervalSpinner.setSelection(2) // Default to 10s
        intervalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                periodicIntervalSeconds = getIntervalSeconds(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        locationProvider = LocationProvider(this)
        periodicButton.setOnClickListener {
            if (periodicJob == null) {
                startPeriodicCapture()
                periodicButton.text = "Stop Periodic Capture"
            } else {
                stopPeriodicCapture()
                periodicButton.text = "Start Periodic Capture"
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                // Used to bind the lifecycle of cameras to the lifecycle owner
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                // Image capture
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val viewPort =
                    ViewPort.Builder(Rational(1, 1), previewView.display.rotation).build()

                val useCaseGroup = UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(imageCapture!!)
                    .setViewPort(viewPort)
                    .build()


                // Select back camera as default
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

                statusText.isVisible = false
                captureButton.isEnabled = true

            } catch (exc: Exception) {
                Log.e("Camera", "Use case binding failed", exc)
                statusText.isVisible = true
                statusText.text = "Camera initialization failed: ${exc.message}"
                captureButton.isEnabled = false
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto(periodic: Boolean = false) {
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            externalMediaDirs.firstOrNull(),
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    cropImageToSquare(photoFile)
                    if (periodic) {
                        processAndSendImage(photoFile)
                    } else {
                        // Always send image to backend, even for manual photo
                        processAndSendImage(photoFile)
                    }
                    runOnUiThread {
                        Toast.makeText(baseContext, "Kvadratna slika shranjena", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onError(exc: ImageCaptureException) {
                    Log.e("Camera", "Photo capture failed: ${exc.message}", exc)
                }
            }
        )
    }

    private fun cropImageToSquare(file: File) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)

        // Določimo krajšo stranico
        val size = if (bitmap.width < bitmap.height) bitmap.width else bitmap.height

        // Izračunamo sredino
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2

        // Ustvarimo kvadratno bitmapo
        val squareBitmap = Bitmap.createBitmap(bitmap, x, y, size, size)

        // Shranimo nazaj v isto datoteko
        val out = FileOutputStream(file)
        squareBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        out.flush()
        out.close()

        bitmap.recycle()
        squareBitmap.recycle()
    }

    private fun startPeriodicCapture() {
        locationProvider.startLocationUpdates()
        periodicJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                withContext(Dispatchers.Main) {
                    takePhoto(periodic = true)
                }
                delay(periodicIntervalSeconds * 1000L)
            }
        }
    }

    private fun stopPeriodicCapture() {
        periodicJob?.cancel()
        periodicJob = null
        locationProvider.stopLocationUpdates()
    }

    private fun processAndSendImage(file: File) {
        // Resize to 640x480, convert to JPEG, encode to Base64
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val resized = Bitmap.createScaledBitmap(bitmap, 640, 480, true)
        val baos = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val imageBytes = baos.toByteArray()
        val imageBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        bitmap.recycle()
        resized.recycle()
        // Get location
        val location = locationProvider.getCurrentLocation()
        // Get deviceId
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        // Prepare JSON
        val json = buildJsonPayload(imageBase64, location, deviceId)
        // Send to backend
        sendImageToBackend(json)
    }

    private fun buildJsonPayload(imageBase64: String, location: com.example.closy.model.LocationData?, deviceId: String): String {
        val locJson = if (location != null) {
            """{"latitude":${location.latitude},"longitude":${location.longitude},"altitude":${location.altitude?:0},"accuracy":${location.accuracy?:0}}"""
        } else {
            "null"
        }
        val timestamp = System.currentTimeMillis()
        return """{"timestamp":$timestamp,"location":$locJson,"imageBase64":"$imageBase64","deviceId":"$deviceId"}"""
    }

    private fun sendImageToBackend(json: String) {
        val prefs = getSharedPreferences("ClosyPreferences", MODE_PRIVATE)
        val serverUrl = prefs.getString("server_url", "http://10.0.2.2:5000/api") ?: "http://10.0.2.2:5000/api"
        val url = serverUrl.replace("/api.*$".toRegex(), "/api/camera-images")
        val body = json.toRequestBody(JSON)
        val request = Request.Builder().url(url).post(body).build()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.i("Camera", "Image sent successfully: ${response.body?.string()}")
                    } else {
                        Log.e("Camera", "Failed to send image: ${response.code} - ${response.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("Camera", "Exception sending image: ${e.message}")
            }
        }
    }

    private fun setupIntervalSpinner(spinner: Spinner) {
        val intervals = arrayOf("1s", "5s", "10s", "30s", "1m", "5m", "10m")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, intervals)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun getIntervalSeconds(position: Int): Int {
        return when (position) {
            0 -> 1      // 1s
            1 -> 5      // 5s
            2 -> 10     // 10s
            3 -> 30     // 30s
            4 -> 60     // 1m
            5 -> 300    // 5m
            6 -> 600    // 10m
            else -> 10
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Camera permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        stopPeriodicCapture()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
