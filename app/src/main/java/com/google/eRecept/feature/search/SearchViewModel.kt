package com.google.eRecept.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.data.model.Medication
import com.google.eRecept.data.model.Patient
import com.google.eRecept.data.model.Recipe
import com.google.eRecept.feature.search.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        private val repository: SearchRepository,
    ) : ViewModel() {
        private val _allPatients = MutableStateFlow<List<Patient>>(emptyList())
        private val _allMedications = MutableStateFlow<List<Medication>>(emptyList())
        private val _allRecipes = MutableStateFlow<List<Recipe>>(emptyList())
        val allRecipes: StateFlow<List<Recipe>> = _allRecipes.asStateFlow()

        private val _patientResults = MutableStateFlow<List<Patient>>(emptyList())
        val patientResults: StateFlow<List<Patient>> = _patientResults.asStateFlow()

        private val _medicationResults = MutableStateFlow<List<Medication>>(emptyList())
        val medicationResults: StateFlow<List<Medication>> = _medicationResults.asStateFlow()

        private val _filteredRecipes = MutableStateFlow<List<Recipe>>(emptyList())
        val filteredRecipes: StateFlow<List<Recipe>> = _filteredRecipes.asStateFlow()

        private val _isSearching = MutableStateFlow(false)
        val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        private var currentQuery = ""
        private var currentTabIndex = 0

        init {
            loadInitialData()
        }

        private fun loadInitialData() {
            val doctorId = repository.currentUserId ?: return

            _isLoading.value = true

            viewModelScope.launch {
                repository.getRecentRecipes(doctorId).collect { list ->
                    _allRecipes.value = list
                    if (currentTabIndex == 2 && currentQuery.isBlank()) {
                        _filteredRecipes.value = list
                    }

                    val patientsFromRecipes =
                        list.map {
                            Patient(it.patient_iin, it.patient_name, "", "", "")
                        }
                    updateDoctorPatients(patientsFromRecipes)
                }
            }

            viewModelScope.launch {
                try {
                    val patientsFromApps = repository.getDoctorPatients(doctorId)
                    updateDoctorPatients(patientsFromApps)

                    val meds = repository.getAllMedications(limit = 200, offset = 0)
                    _allMedications.value = meds
                    _medicationResults.value = meds
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _isLoading.value = false
                }
            }
        }

        private fun updateDoctorPatients(newPatients: List<Patient>) {
            val currentList = _allPatients.value
            val combined = (currentList + newPatients).distinctBy { it.iin }
            _allPatients.value = combined

            if (currentQuery.isBlank()) {
                _patientResults.value = combined
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
                    _patientResults.value = _allPatients.value
                    _medicationResults.value = _allMedications.value
                    _filteredRecipes.value = _allRecipes.value
                    return@launch
                }

                val lowerQuery = query.lowercase()
                when (tabIndex) {
                    0 -> {
                        _patientResults.value =
                            _allPatients.value.filter {
                                it.full_name.lowercase().contains(lowerQuery) || it.iin.contains(query)
                            }
                    }

                    1 -> {
                        _isSearching.value = true
                        _medicationResults.value = repository.searchMedications(query)
                        _isSearching.value = false
                    }

                    2 -> {
                        _filteredRecipes.value =
                            _allRecipes.value.filter { recipe ->
                                recipe.id.lowercase().contains(lowerQuery) ||
                                    recipe.patient_name.lowercase().contains(lowerQuery) ||
                                    recipe.patient_iin.contains(query)
                            }
                    }
                }
            }
        }

        fun refresh() {
            viewModelScope.launch {
                _isRefreshing.value = true
                try {
                    val doctorId = repository.currentUserId
                    if (doctorId != null) {
                        // Force reload data
                        val list = repository.getRecentRecipes(doctorId).first()
                        _allRecipes.value = list
                        if (currentTabIndex == 2 && currentQuery.isBlank()) {
                            _filteredRecipes.value = list
                        }
                        updateDoctorPatients(list.map { Patient(it.patient_iin, it.patient_name, "", "", "") })

                        val patientsFromApps = repository.getDoctorPatients(doctorId)
                        updateDoctorPatients(patientsFromApps)

                        val meds = repository.getAllMedications(limit = 200, offset = 0)
                        _allMedications.value = meds
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _isRefreshing.value = false
                }
            }
        }
    }