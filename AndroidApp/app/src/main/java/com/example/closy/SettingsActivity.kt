package com.example.closy

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Locale

/**
 * Settings Activity
 *
 * Allows users to configure app settings:
 * - Language selection (English/Slovenian)
 * - Dark mode toggle
 * - Notifications toggle
 * - About information
 */
//test
class SettingsActivity : AppCompatActivity() {

    private lateinit var languageRadioGroup: RadioGroup
    private lateinit var radioEnglish: RadioButton
    private lateinit var radioSlovenian: RadioButton
    private lateinit var darkModeSwitch: SwitchMaterial
    private lateinit var notificationsSwitch: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved language before setting content view
        applySavedLanguage()

        // Apply saved dark mode preference
        applySavedDarkMode()

        setContentView(R.layout.activity_settings)

        // Setup Toolbar with back button
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = getString(R.string.settings)

        // Initialize views
        languageRadioGroup = findViewById(R.id.languageRadioGroup)
        radioEnglish = findViewById(R.id.radioEnglish)
        radioSlovenian = findViewById(R.id.radioSlovenian)
        darkModeSwitch = findViewById(R.id.darkModeSwitch)
        notificationsSwitch = findViewById(R.id.notificationsSwitch)

        // Load saved preferences
        loadLanguagePreference()
        loadDarkModePreference()
        loadNotificationsPreference()

        // Setup language change listener
        languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioEnglish -> setLanguage("en")
                R.id.radioSlovenian -> setLanguage("sl")
            }
        }

        // Setup dark mode toggle listener
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            setDarkMode(isChecked)
        }

        // Setup notifications toggle listener
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            setNotifications(isChecked)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadLanguagePreference() {
        val prefs = getSharedPreferences("ClosyPreferences", Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString("app_language", "en") ?: "en"

        when (savedLanguage) {
            "en" -> radioEnglish.isChecked = true
            "sl" -> radioSlovenian.isChecked = true
        }
    }

    private fun loadDarkModePreference() {
        val prefs = getSharedPreferences("ClosyPreferences", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        darkModeSwitch.isChecked = isDarkMode
    }

    private fun loadNotificationsPreference() {
        val prefs = getSharedPreferences("ClosyPreferences", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        notificationsSwitch.isChecked = notificationsEnabled
    }

    private fun setLanguage(languageCode: String) {
        // Save language preference
        val prefs = getSharedPreferences("ClosyPreferences", Context.MODE_PRIVATE)
        val currentLanguage = prefs.getString("app_language", "en")

        // Only update if language changed
        if (currentLanguage != languageCode) {
            prefs.edit().putString("app_language", languageCode).apply()

            // Apply language
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val config = Configuration(resources.configuration)
            config.setLocale(locale)

            resources.updateConfiguration(config, resources.displayMetrics)

            // Recreate activity to apply changes
            recreate()
        }
    }

    private fun setDarkMode(enabled: Boolean) {
        val prefs = getSharedPreferences("ClosyPreferences", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("dark_mode", enabled).apply()

        // Apply dark mode immediately
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun setNotifications(enabled: Boolean) {
        val prefs = getSharedPreferences("ClosyPreferences", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()

        // Here you could add logic to enable/disable notifications
        // For now, just save the preference
    }

    private fun applySavedLanguage() {
        val prefs = getSharedPreferences("ClosyPreferences", Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString("app_language", "en") ?: "en"

        val locale = Locale(savedLanguage)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun applySavedDarkMode() {
        val prefs = getSharedPreferences("ClosyPreferences", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}

