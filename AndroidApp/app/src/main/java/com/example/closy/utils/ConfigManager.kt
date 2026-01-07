package com.example.closy.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Configuration manager for storing app settings
 */
class ConfigManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "ClosyPreferences"
        private const val KEY_SERVER_URL = "server_url"
        private const val DEFAULT_SERVER_URL = "http://your-server.com/api/sensor-data"
    }

    /**
     * Get server URL
     */
    fun getServerUrl(): String {
        return sharedPreferences.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
    }

    /**
     * Set server URL
     */
    fun setServerUrl(url: String) {
        sharedPreferences.edit().putString(KEY_SERVER_URL, url).apply()
    }

    /**
     * Get sensor interval
     */
    fun getSensorInterval(sensorType: String): Int {
        return sharedPreferences.getInt("${sensorType}_interval", 60)
    }

    /**
     * Set sensor interval
     */
    fun setSensorInterval(sensorType: String, interval: Int) {
        sharedPreferences.edit().putInt("${sensorType}_interval", interval).apply()
    }

    /**
     * Get sensor enabled state
     */
    fun isSensorEnabled(sensorType: String): Boolean {
        return sharedPreferences.getBoolean("${sensorType}_enabled", false)
    }

    /**
     * Set sensor enabled state
     */
    fun setSensorEnabled(sensorType: String, enabled: Boolean) {
        sharedPreferences.edit().putBoolean("${sensorType}_enabled", enabled).apply()
    }
}

