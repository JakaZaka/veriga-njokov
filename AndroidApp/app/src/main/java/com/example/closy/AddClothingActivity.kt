package com.example.closy

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.closy.databinding.ActivityAddClothingBinding
import com.example.closy.utils.*
import java.io.File
import java.util.Locale

class AddClothingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddClothingBinding
    private val viewModel: ClothingViewModel by viewModels()

    private var selectedImageUri: Uri? = null
    private var currentPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddClothingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        setupClickListeners()
    }

    private fun setupUI() {
        // 1. Nastavitev barv s krogci
        val colorItems = listOf("Black", "White", "Gray", "Red", "Blue", "Beige", "Brown", "Green")
        val colorHex = listOf("#000000", "#FFFFFF", "#808080", "#FF0000", "#0000FF", "#F5F5DC", "#A52A2A", "#008000")
        binding.colorAutoComplete.setAdapter(IconAdapter(this, colorItems, colorHex))

        // 2. Nastavitev kategorij z ikonami
        val categories = listOf("Tops", "Bottoms", "Dresses", "Outerwear", "Shoes")
        val categoryIcons = listOf(
            R.drawable.ic_tshirt,
            R.drawable.ic_pants,
            R.drawable.ic_dress,
            R.drawable.ic_outerwear,
            R.drawable.ic_shoes
        )
        binding.categoryAutoComplete.setAdapter(IconAdapter(this, categories, categoryIcons))

        binding.formContainer.visibility = View.GONE
    }

    private fun setupObservers() {
        viewModel.classificationResult.observe(this) { result ->
            populateFormWithAI(result)
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
        }

        viewModel.error.observe(this) { error ->
            error?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
        }

        viewModel.createdItem.observe(this) {
            Toast.makeText(this, "Oblačilo shranjeno v digitalni dvojček!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun populateFormWithAI(data: ClassificationData) {
        binding.formContainer.visibility = View.VISIBLE

        // Ime
        val colorName = data.primaryColor.replaceFirstChar { it.uppercase() }
        binding.etName.setText("$colorName ${data.classification}")

        binding.etSubCategory.setText(data.classification.replaceFirstChar { it.uppercase() })

        // AutoComplete polja (false prepreči filtriranje seznama)
        binding.colorAutoComplete.setText(data.primaryColor.replaceFirstChar { it.uppercase() }, false)

        val matchedCat = when(data.category.lowercase()) {
            "tops" -> "Tops"
            "bottoms" -> "Bottoms"
            "dresses" -> "Dresses"
            "outerwear" -> "Outerwear"
            "shoes" -> "Shoes"
            else -> "Tops"
        }
        binding.categoryAutoComplete.setText(matchedCat, false)

        // Pametna izbira letnih časov
        resetChips()
        if (data.category.contains("outerwear")) {
            binding.chipWinter.isChecked = true
            binding.chipFall.isChecked = true
        }

        binding.tvAIConfidence.apply {
            text = "AI Confidence: ${(data.confidence * 100).toInt()}%"
            visibility = View.VISIBLE
        }
    }

    private fun resetChips() {
        binding.chipSpring.isChecked = false
        binding.chipSummer.isChecked = false
        binding.chipFall.isChecked = false
        binding.chipWinter.isChecked = false
    }

    private fun saveClothingItem() {
        val uri = selectedImageUri ?: return
        val name = binding.etName.text.toString().trim()

        if (name.isEmpty()) {
            binding.etName.error = "Ime je obvezno"
            return
        }

        val overrides = ClothingItemOverrides(
            name = name,
            category = binding.categoryAutoComplete.text.toString().lowercase(),
            color = binding.colorAutoComplete.text.toString().lowercase(),
            season = getSelectedSeasonsList(),
            subCategory = binding.etSubCategory.text.toString().ifEmpty { null },
            notes = binding.etNotes.text.toString().ifEmpty { null }
        )

        viewModel.createClothingItem(uri, getAuthToken(), overrides)
    }

    private fun getSelectedSeasonsList(): List<String> {
        val seasons = mutableListOf<String>()
        if (binding.chipSpring.isChecked) seasons.add("spring")
        if (binding.chipSummer.isChecked) seasons.add("summer")
        if (binding.chipFall.isChecked) seasons.add("fall")
        if (binding.chipWinter.isChecked) seasons.add("winter")
        return if (seasons.isEmpty()) listOf("all") else seasons
    }

    // --- Kamera & Galerija ---
    private fun setupClickListeners() {
        binding.btnCamera.setOnClickListener { checkCameraPermissionAndOpen() }
        binding.btnGallery.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.btnSave.setOnClickListener { saveClothingItem() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) openCamera()
        else requestCameraPermission.launch(Manifest.permission.CAMERA)
    }

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) openCamera() }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) currentPhotoUri?.let { uri -> displayImage(uri); viewModel.classifyImage(uri) }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedImageUri = it; displayImage(it); viewModel.classifyImage(it) }
    }

    private fun openCamera() {
        val photoFile = File.createTempFile("clothing_", ".jpg", cacheDir)
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        currentPhotoUri = uri
        selectedImageUri = uri
        takePictureLauncher.launch(uri)
    }

    private fun displayImage(uri: Uri) {
        binding.imageView.setImageURI(uri)
        binding.imageView.visibility = View.VISIBLE
    }

    private fun getAuthToken() = getSharedPreferences("auth", MODE_PRIVATE).getString("token", "") ?: ""
}