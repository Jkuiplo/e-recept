package com.google.eRecept.feature.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.data.model.Medication
import com.google.eRecept.data.model.MedicationItem
import com.google.eRecept.data.model.Recipe
import com.google.eRecept.feature.recipe.repository.RecipeRepository
import com.google.eRecept.data.network.dto.RecipeItemDto
import com.google.eRecept.data.network.dto.UpdateRecipeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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

        private val _isRevoking = MutableStateFlow(false)
        val isRevoking = _isRevoking.asStateFlow()

        private val _editingRecipeId = MutableStateFlow<String?>(null)
        val editingRecipeId = _editingRecipeId.asStateFlow()

        private val _isCreating = MutableStateFlow(false)
        val isCreating = _isCreating.asStateFlow()

        init {
            loadRecipes()
        }

        fun openCreateSheet(iin: String? = null) {
            if (iin != null) {
                _draftPatientIin.value = iin
            }
            _showCreateSheet.value = true
        }

        fun openEditSheet(recipe: Recipe) {
            _editingRecipeId.value = recipe.id
            _draftPatientIin.value = recipe.patient_iin
            _draftNotes.value = recipe.notes
            _draftMedications.value = recipe.medications.ifEmpty { listOf(MedicationItem()) }

            val diffDays = ((recipe.expire_date - recipe.date) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
            val closestOption = listOf(10, 15, 30, 60).minByOrNull { Math.abs(it - diffDays) } ?: 30
            _draftExpireDays.value = closestOption

            _showCreateSheet.value = true
        }

        fun closeCreateSheet() {
            _showCreateSheet.value = false
            _editingRecipeId.value = null
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
            _editingRecipeId.value = null
        }

        private fun loadRecipes() {
            repository.currentUserId?.let { doctorId ->
                viewModelScope.launch {
                    _isLoading.value = true
                    repository.getRecentRecipes(doctorId).collect {
                        _recipes.value = it
                        _isLoading.value = false
                    }
                }
            }
        }

        fun refresh() {
            viewModelScope.launch {
                _isRefreshing.value = true
                try {
                    repository.currentUserId?.let { doctorId ->
                        val list = repository.getRecentRecipes(doctorId).first()
                        _recipes.value = list
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _isRefreshing.value = false
                }
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

        fun revokeRecipe(
            recipeId: String,
            onSuccess: () -> Unit,
        ) {
            viewModelScope.launch {
                _isRevoking.value = true
                val success = repository.revokeRecipe(recipeId)
                _isRevoking.value = false
                if (success) {
                    onSuccess()
                }
            }
        }

        fun saveRecipe(patientName: String) {
            if (_isCreating.value) return

            val doctorId = repository.currentUserId ?: return
            val iin = _draftPatientIin.value
            val meds = _draftMedications.value.filter { it.name.isNotBlank() }
            val notes = _draftNotes.value
            val expireDays = _draftExpireDays.value
            val editId = _editingRecipeId.value

            viewModelScope.launch {
                _isCreating.value = true
                try {
                    if (editId != null) {
                        val itemsDto =
                            meds.map {
                                RecipeItemDto(
                                    medicationId = it.id.ifBlank { null },
                                    medicationName = it.name,
                                    dosageValue = it.dosageValue,
                                    dosageUnit = it.dosageUnit,
                                    frequency = it.frequency,
                                    durationValue = it.durationValue,
                                    durationUnit = it.durationUnit,
                                    note = it.note,
                                )
                            }
                        val request =
                            UpdateRecipeRequest(
                                notes = notes,
                                expireDays = expireDays,
                                items = itemsDto,
                            )
                        repository.updateRecipe(editId, request)
                    } else {
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
                                expire_date = expireTime,
                                medications = meds,
                                notes = notes,
                                status = "Активен", // Обязательно указываем статус
                            )
                        repository.createRecipe(recipe)
                    }
                    clearDraft()
                    closeCreateSheet()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _isCreating.value = false
                }
            }
        }
    }