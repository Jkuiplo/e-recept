package com.google.eRecept.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.data.Medication
import com.google.eRecept.data.Patient
import com.google.eRecept.data.Recipe
import com.google.eRecept.data.mockRepository.SearchRepository
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

        private val _filteredRecipes = MutableStateFlow<List<Recipe>>(emptyList())
        val filteredRecipes: StateFlow<List<Recipe>> = _filteredRecipes.asStateFlow()

        init {
            loadRecipes()
            loadInitialMedications()
        }

        private fun loadRecipes() {
            repository.currentUserId?.let { doctorId ->
                viewModelScope.launch {
                    repository.getRecentRecipes(doctorId).collect { list ->
                        _allRecipes.value = list
                        applyRecipeFilter(currentQuery)
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
                    if (tabIndex == 2) _filteredRecipes.value = _allRecipes.value
                    return@launch
                }

                when (tabIndex) {
                    0 -> _patientResults.value = repository.searchPatients(query)
                    1 -> _medicationResults.value = repository.searchMedications(query)
                    2 -> applyRecipeFilter(query)
                }
            }
        }

        private fun applyRecipeFilter(query: String) {
            val lowerQuery = query.lowercase()
            _filteredRecipes.value =
                _allRecipes.value.filter { recipe ->
                    recipe.id.lowercase().contains(lowerQuery) ||
                        recipe.patient_name.lowercase().contains(lowerQuery) ||
                        recipe.patient_iin.contains(query)
                }
        }
    }
