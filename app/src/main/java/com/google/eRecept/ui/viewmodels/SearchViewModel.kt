package com.google.eRecept.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.data.FirebaseRepository
import com.google.eRecept.data.Medication
import com.google.eRecept.data.Patient
import com.google.eRecept.data.Recipe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: FirebaseRepository = FirebaseRepository()) : ViewModel() {
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

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(500)
            if (currentTabIndex == 2) {
                // На третьей вкладке просто перезагружаем всю историю
                loadRecipes()
            } else {
                // На остальных вкладках повторяем поиск с текущим запросом
                search(currentQuery, currentTabIndex)
            }
            _isRefreshing.value = false
        }
    }

    fun search(query: String, tabIndex: Int) {
        currentQuery = query
        currentTabIndex = tabIndex

        if (query.isBlank()) {
            _patientResults.value = emptyList()
            _medicationResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            when (tabIndex) {
                0 -> _patientResults.value = repository.searchPatients(query)
                1 -> _medicationResults.value = repository.searchMedications(query)
            }
            _isSearching.value = false
        }
    }
}