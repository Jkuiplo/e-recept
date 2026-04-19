package com.google.eRecept.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.data.Medication
import com.google.eRecept.data.Patient
import com.google.eRecept.data.Recipe
import com.google.eRecept.data.mockRepository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
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

        private var currentQuery = ""
        private var currentTabIndex = 0

        init {
            loadInitialData()
        }

        private fun loadInitialData() {
            val doctorId = repository.currentUserId ?: return

            _isSearching.value = true

            // 1. История рецептов (Flow)
            viewModelScope.launch {
                repository.getRecentRecipes(doctorId).collect { list ->
                    _allRecipes.value = list
                    // Если мы в истории и поиск пустой — обновляем экран
                    if (currentTabIndex == 2 && currentQuery.isBlank()) {
                        _filteredRecipes.value = list
                    }

                    // ВАЛИДАЦИЯ: Добавляем пациентов из рецептов в общий список врача
                    val patientsFromRecipes =
                        list.map {
                            Patient(it.patient_iin, it.patient_name, "", "", "")
                        }
                    updateDoctorPatients(patientsFromRecipes)
                }
            }

            // 2. Пациенты из приемов и Препараты
            viewModelScope.launch {
                try {
                    // Загружаем пациентов из приемов
                    val patientsFromApps = repository.getDoctorPatients(doctorId)
                    updateDoctorPatients(patientsFromApps)

                    // Загружаем препараты (пустой запрос для дефолтного списка)
                    val meds = repository.searchMedications("")
                    _allMedications.value = meds
                    // Сразу пушим в UI, чтобы список не был пустым при переключении
                    _medicationResults.value = meds
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _isSearching.value = false
                }
            }
        }

        // Вспомогательный метод для синхронизации списка "своих" пациентов
        private fun updateDoctorPatients(newPatients: List<Patient>) {
            val currentList = _allPatients.value
            val combined = (currentList + newPatients).distinctBy { it.iin }
            _allPatients.value = combined

            // Если поиск пустой, обновляем то, что видит юзер
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
                // ФИКС: Если запрос стерли — возвращаем ПОЛНЫЕ списки для всех вкладок
                if (query.isBlank()) {
                    _patientResults.value = _allPatients.value
                    _medicationResults.value = _allMedications.value
                    _filteredRecipes.value = _allRecipes.value
                    return@launch
                }

                val lowerQuery = query.lowercase()
                when (tabIndex) {
                    0 -> { // Локальный поиск по "своим" пациентам
                        _patientResults.value =
                            _allPatients.value.filter {
                                it.full_name.lowercase().contains(lowerQuery) || it.iin.contains(query)
                            }
                    }

                    1 -> { // Поиск препаратов через API
                        _isSearching.value = true
                        _medicationResults.value = repository.searchMedications(query)
                        _isSearching.value = false
                    }

                    2 -> { // Локальный поиск по истории
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
                delay(500)
                loadInitialData()
                _isRefreshing.value = false
            }
        }
    }
