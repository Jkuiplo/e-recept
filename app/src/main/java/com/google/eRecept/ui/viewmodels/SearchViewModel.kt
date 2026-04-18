package com.google.eRecept.ui.viewmodels

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.data.Medication
import com.google.eRecept.data.Patient
import com.google.eRecept.data.Recipe
import com.google.eRecept.data.mockRepository.SearchRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        private val repository: SearchRepository,
    ) : ViewModel() {
        private val _patientResults = MutableStateFlow<List<Patient>>(emptyList())
        val patientResults: StateFlow<List<Patient>> = _patientResults.asStateFlow()

        private val _medicationResults = MutableStateFlow<List<Medication>>(emptyList())
        val medicationResults: StateFlow<List<Medication>> = _medicationResults.asStateFlow()

        private val _allRecipes = MutableStateFlow<List<Recipe>>(emptyList())
        val allRecipes: StateFlow<List<Recipe>> = _allRecipes.asStateFlow()

        private val _isSearching = MutableStateFlow(false)
        val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        private var currentQuery = ""
        private var currentTabIndex = 0

        init {
            loadRecipes()
            loadInitialMedications() // Сразу грузим препараты
        }

        private fun loadRecipes() {
            repository.currentUserId?.let { doctorId ->
                viewModelScope.launch {
                    repository.getRecentRecipes(doctorId).collect {
                        _allRecipes.value = it
                    }
                }
            }
        }

        private fun loadInitialMedications() {
            viewModelScope.launch {
                _isSearching.value = true
                _medicationResults.value = repository.searchMedications("")
                _isSearching.value = false
            }
        }

        fun refresh() {
            viewModelScope.launch {
                _isRefreshing.value = true
                delay(500)
                if (currentTabIndex == 2) {
                    loadRecipes()
                } else {
                    search(currentQuery, currentTabIndex)
                }
                _isRefreshing.value = false
            }
        }

        fun search(
            query: String,
            tabIndex: Int,
        ) {
            currentQuery = query
            currentTabIndex = tabIndex

            viewModelScope.launch {
                if (query.isBlank()) {
                    if (tabIndex == 0) _patientResults.value = emptyList()
                    if (tabIndex == 1) {
                        // Если стерли запрос в препаратах — возвращаем полный список
                        _isSearching.value = true
                        _medicationResults.value = repository.searchMedications("")
                        _isSearching.value = false
                    }
                    return@launch
                }

                _isSearching.value = true
                when (tabIndex) {
                    0 -> _patientResults.value = repository.searchPatients(query)
                    1 -> _medicationResults.value = repository.searchMedications(query)
                }
                _isSearching.value = false
            }
        }

        // Для модалки деталей рецепта
        fun generateQrCode(text: String): Bitmap {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap[x, y] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }
            return bitmap
        }
    }
