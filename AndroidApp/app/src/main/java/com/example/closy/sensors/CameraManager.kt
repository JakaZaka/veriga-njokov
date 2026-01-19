package com.example.closy.sensors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.closy.model.CameraData
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Manager for camera image capture using CameraX
 */
//comment
class CameraManager(private val context: Context) {

    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    /**
     * Initialize camera
     */
    fun initializeCamera(lifecycleOwner: LifecycleOwner, callback: (Boolean) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            callback(false)
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                // Build the image capture use case
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                // Select back camera as a default
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Unbind use cases before rebinding
                cameraProvider?.unbindAll()

                // Bind use cases to camera
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageCapture
                )

                callback(true)
            } catch (e: Exception) {
                e.printStackTrace()
                callback(false)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Capture image from camera
     */
    fun captureImage(callback: (CameraData?) -> Unit) {
        val imageCapture = imageCapture ?: run {
            callback(null)
            return
        }

        val photoDir = File(context.filesDir, "photos").apply { if (!exists()) mkdirs() }
        val photoFile = File(photoDir, "photo_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // --- TUKAJ DODAMO OBREZOVANJE NA KVADRAT ---
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                    // Izračunamo dimenzije za kvadrat (vzrejam sredinski del)
                    val size = if (bitmap.width < bitmap.height) bitmap.width else bitmap.height
                    val x = (bitmap.width - size) / 2
                    val y = (bitmap.height - size) / 2

                    val squareBitmap = Bitmap.createBitmap(bitmap, x, y, size, size)

                    // Shranimo obrezano sliko nazaj čez original
                    FileOutputStream(photoFile).use { out ->
                        squareBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }

                    val cameraData = CameraData(
                        imagePath = photoFile.absolutePath,
                        width = size,
                        height = size
                    )
                    callback(cameraData)
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                    callback(null)
                }
            }
        )
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
    }
}

