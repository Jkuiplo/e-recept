package com.google.eRecept.ui.viewmodels

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.data.FirebaseRepository
import com.google.eRecept.data.Medication
import com.google.eRecept.data.MedicationItem
import com.google.eRecept.data.Recipe
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: FirebaseRepository = FirebaseRepository()) : ViewModel() {
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private val _medicationSuggestions = MutableStateFlow<List<Medication>>(emptyList())
    val medicationSuggestions: StateFlow<List<Medication>> = _medicationSuggestions.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadRecipes()
    }

    private fun loadRecipes() {
        repository.currentUserId?.let { doctorId ->
            viewModelScope.launch {
                repository.getRecentRecipes(doctorId).collect {
                    _recipes.value = it
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(500)
            loadRecipes()
            _isRefreshing.value = false
        }
    }

    fun searchMedications(query: String) {
        if (query.isBlank()) {
            _medicationSuggestions.value = emptyList()
            return
        }
        viewModelScope.launch {
            _medicationSuggestions.value = repository.searchMedications(query)
        }
    }

    fun createRecipe(patientIin: String, patientName: String, medications: List<MedicationItem>, notes: String) {
        val doctorId = repository.currentUserId ?: return
        viewModelScope.launch {
            val doctorProfile = repository.getDoctorProfile(doctorId)
            val recipe = Recipe(
                doctor_id = doctorId,
                doctor_name = doctorProfile?.name ?: "Врач",
                patient_iin = patientIin,
                patient_name = patientName,
                medications = medications.filter { it.name.isNotBlank() },
                notes = notes
            )
            repository.createRecipe(recipe)
        }
    }

    fun generateQrCode(text: String): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}