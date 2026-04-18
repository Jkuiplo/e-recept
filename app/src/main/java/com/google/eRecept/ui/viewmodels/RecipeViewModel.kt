package com.google.eRecept.ui.viewmodels

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.data.Medication
import com.google.eRecept.data.MedicationItem
import com.google.eRecept.data.Recipe
import com.google.eRecept.data.repository.RecipeRepository
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
class RecipeViewModel
    @Inject
    constructor(
        private val repository: RecipeRepository,
    ) : ViewModel() {
        private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
        val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

        private val _medicationSuggestions = MutableStateFlow<List<Medication>>(emptyList())
        val medicationSuggestions: StateFlow<List<Medication>> = _medicationSuggestions.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        // --- УПРАВЛЕНИЕ МОДАЛКОЙ И ЧЕРНОВИКОМ ---
        private val _showCreateSheet = MutableStateFlow(false)
        val showCreateSheet = _showCreateSheet.asStateFlow()

        private val _draftPatientIin = MutableStateFlow("")
        val draftPatientIin = _draftPatientIin.asStateFlow()

        private val _draftNotes = MutableStateFlow("")
        val draftNotes = _draftNotes.asStateFlow()

        private val _draftExpireDays = MutableStateFlow(10)
        val draftExpireDays = _draftExpireDays.asStateFlow()

        private val _draftMedications = MutableStateFlow(listOf(MedicationItem()))
        val draftMedications = _draftMedications.asStateFlow()

        // ВЫЗЫВАЙ ЭТУ ФУНКЦИЮ ПРИ ПЕРЕХОДЕ С ГЛАВНОЙ СТРАНИЦЫ
        fun openCreateSheet(iin: String? = null) {
            if (iin != null) {
                _draftPatientIin.value = iin
            }
            _showCreateSheet.value = true
        }

        fun closeCreateSheet() {
            _showCreateSheet.value = false
        }

        fun updateDraftIin(iin: String) {
            _draftPatientIin.value = iin
        }

        fun updateDraftExpireDays(days: Int) {
            _draftExpireDays.value = days
        }

        fun updateDraftNotes(notes: String) {
            _draftNotes.value = notes
        }

        fun updateDraftMedications(meds: List<MedicationItem>) {
            _draftMedications.value = meds
        }

        fun clearDraft() {
            _draftPatientIin.value = ""
            _draftNotes.value = ""
            _draftMedications.value = listOf(MedicationItem())
            _draftExpireDays.value = 10
        }
        // ------------------------------------------------------------------------

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

        fun createRecipe(patientName: String) {
            val doctorId = repository.currentUserId ?: return
            val iin = _draftPatientIin.value
            val meds = _draftMedications.value.filter { it.name.isNotBlank() }
            val notes = _draftNotes.value
            val expireDays = _draftExpireDays.value

            viewModelScope.launch {
                val doctorProfile = repository.getDoctorProfile(doctorId)
                val currentTime = System.currentTimeMillis()
                val expireTime = currentTime + (expireDays * 24L * 60L * 60L * 1000L)

                val recipe =
                    Recipe(
                        doctor_id = doctorId,
                        doctor_name = doctorProfile?.name ?: "Врач",
                        patient_iin = iin,
                        patient_name = patientName,
                        date = currentTime,
                        expire_date = expireTime, // Сохраняем дату истечения
                        medications = meds,
                        notes = notes,
                    )
                repository.createRecipe(recipe)
                clearDraft()
                closeCreateSheet()
            }
        }

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
