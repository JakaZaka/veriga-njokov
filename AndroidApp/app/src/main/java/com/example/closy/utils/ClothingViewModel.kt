package com.example.closy.utils

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ClothingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ClothingRepository()

    // LiveData for UI
    private val _classificationResult = MutableLiveData<ClassificationData>()
    val classificationResult: LiveData<ClassificationData> = _classificationResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String> = _error as LiveData<String>

    private val _createdItem = MutableLiveData<CreateClothingData>()
    val createdItem: LiveData<CreateClothingData> = _createdItem

    /**
     * Klasificiraj sliko
     */
    fun classifyImage(imageUri: Uri) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = repository.classifyImage(imageUri, getApplication())

            result.onSuccess { data ->
                _classificationResult.value = data
                _loading.value = false
            }.onFailure { exception ->
                _error.value = exception.message ?: "Classification failed"
                _loading.value = false
            }
        }
    }

    /**
     * Ustvari clothing item
     */
    fun createClothingItem(
        imageUri: Uri,
        token: String,
        overrides: ClothingItemOverrides? = null
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = repository.createClothingWithAI(
                imageUri,
                getApplication(),
                token,
                overrides
            )

            result.onSuccess { data ->
                _createdItem.value = data
                _loading.value = false
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to create item"
                _loading.value = false
            }
        }
    }

    /**
     * Posodobi clothing item
     */
    fun updateClothingItem(id: String, token: String, item: ClothingItem) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = repository.updateClothingItem(id, token, item)

            result.onSuccess {
                _loading.value = false
                // Lahko triggerneš navigation ali refresh
            }.onFailure { exception ->
                _error.value = exception.message ?: "Update failed"
                _loading.value = false
            }
        }
    }
}