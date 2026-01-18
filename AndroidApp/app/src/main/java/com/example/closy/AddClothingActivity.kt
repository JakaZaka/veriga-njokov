package com.example.closy

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.closy.databinding.ActivityAddClothingBinding
import com.example.closy.utils.ClassificationData
import com.example.closy.utils.ClothingItemOverrides
import com.example.closy.utils.ClothingViewModel
import java.io.File
import java.util.Locale

class AddClothingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddClothingBinding
    private val viewModel: ClothingViewModel by viewModels()

    private var selectedImageUri: Uri? = null
    private var currentPhotoUri: Uri? = null // Popravljeno: Uri mora biti nullable
    private var aiResults: ClassificationData? = null

    // Mappings
    private val categories = listOf("tops", "bottoms", "dresses", "outerwear", "shoes", "accessories", "other")
    private val colors = listOf("black", "white", "gray", "red", "blue", "green", "yellow", "pink", "purple", "brown", "beige", "other")
    private val sizes = listOf("XS", "S", "M", "L", "XL", "2XL", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "other")

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                selectedImageUri = uri
                displayImage(uri)
                classifyImage(uri)
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            displayImage(it)
            classifyImage(it)
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddClothingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        setupClickListeners()
    }

    private fun setupUI() {
        binding.categorySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        binding.colorSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, colors)
        binding.sizeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sizes)

        binding.formContainer.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    private fun setupObservers() {
        viewModel.classificationResult.observe(this) { result ->
            aiResults = result
            populateFormWithAI(result)
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.createdItem.observe(this) {
            Toast.makeText(this, "Clothing item saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnCamera.setOnClickListener { checkCameraPermissionAndOpen() }
        binding.btnGallery.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.btnSave.setOnClickListener { saveClothingItem() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val photoFile = File.createTempFile("clothing_", ".jpg", cacheDir)
        val uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            photoFile
        )
        currentPhotoUri = uri
        takePictureLauncher.launch(uri)
    }

    private fun displayImage(uri: Uri) {
        binding.imageView.setImageURI(uri)
        binding.imageView.visibility = View.VISIBLE
    }

    private fun classifyImage(uri: Uri) {
        viewModel.classifyImage(uri)
    }


    private fun populateFormWithAI(data: ClassificationData) {
        // Prikažemo obrazec, ko AI vrne rezultate
        binding.formContainer.visibility = View.VISIBLE

        // AI predlaga ime (uporabnik ga lahko ročno pobriše/spremeni v etName)
        val suggestedName = "${data.primaryColor.replaceFirstChar { it.uppercase() }} ${data.classification}"
        binding.etName.setText(suggestedName)

        // Spinnerji se nastavijo na AI rezultate, a ostanejo odklenjeni za uporabnika
        val catIdx = categories.indexOf(data.category.lowercase())
        if (catIdx >= 0) binding.categorySpinner.setSelection(catIdx)

        val colorIdx = colors.indexOf(data.primaryColor.lowercase())
        if (colorIdx >= 0) binding.colorSpinner.setSelection(colorIdx)

        binding.etSubCategory.setText(data.subCategory)

        // Prikaz samozavesti AI
        binding.tvAIConfidence.text = "AI Confidence: ${(data.confidence * 100).toInt()}%"
        binding.tvAIConfidence.visibility = View.VISIBLE
    }

    private fun saveClothingItem() {
        val uri = selectedImageUri ?: return

        // TUKAJ POEREMO TRENUTNE VREDNOSTI IZ UI (tiste, ki jih je uporabnik morda popravil)
        val overrides = ClothingItemOverrides(
            name = binding.etName.text.toString(),
            category = binding.categorySpinner.selectedItem.toString(),
            subCategory = binding.etSubCategory.text.toString().ifEmpty { null },
            color = binding.colorSpinner.selectedItem.toString(),
            size = binding.sizeSpinner.selectedItem.toString(),
            season = getSelectedSeasonsList(), // Funkcija spodaj
            notes = binding.etNotes.text.toString().ifEmpty { null }
        )

        // Pošljemo v bazo
        viewModel.createClothingItem(uri, getAuthToken(), overrides)
    }

    private fun getAuthToken(): String {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        return prefs.getString("token", "") ?: ""
    }

    private fun getSelectedSeasonsList(): List<String> {
        val seasons = mutableListOf<String>()
        if (binding.cbSpring.isChecked) seasons.add("spring")
        if (binding.cbSummer.isChecked) seasons.add("summer")
        if (binding.cbFall.isChecked) seasons.add("fall")
        if (binding.cbWinter.isChecked) seasons.add("winter")
        return if (seasons.isEmpty()) listOf("all") else seasons
    }
}