package com.example.closy.sensors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.core.app.ActivityCompat
import com.example.closy.model.AudioData
import java.io.File
import java.io.IOException

/**
 * Manager for audio recording from microphone
 */
class AudioRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false

    /**
     * Start recording audio
     */
    fun startRecording(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        if (isRecording) {
            return false
        }

        try {
            // Create output file
            val audioDir = File(context.filesDir, "audio")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }
            outputFile = File(audioDir, "audio_${System.currentTimeMillis()}.3gp")

            // Setup MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile?.absolutePath)

                prepare()
                start()
                isRecording = true
            }

            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Stop recording audio and return AudioData
     */
    fun stopRecording(): AudioData? {
        if (!isRecording || mediaRecorder == null) {
            return null
        }

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false

            // Get max amplitude (simplified - in real app, you'd calculate RMS during recording)
            val amplitude = mediaRecorder?.maxAmplitude?.toDouble() ?: 0.0

            return AudioData(
                amplitude = amplitude,
                filePath = outputFile?.absolutePath
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Get current recording status
     */
    fun isRecording(): Boolean = isRecording

    /**
     * Clean up resources
     */
    fun cleanup() {
        if (isRecording) {
            stopRecording()
        }
    }
}

