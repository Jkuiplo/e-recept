package com.google.eRecept.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.data.Appointment
import com.google.eRecept.data.Patient
import com.google.eRecept.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val repository: HomeRepository,
    ) : ViewModel() {
        private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
        val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

        private val _searchPatientResult = MutableStateFlow<Patient?>(null)
        val searchPatientResult: StateFlow<Patient?> = _searchPatientResult.asStateFlow()

        private val _isSearching = MutableStateFlow(false)
        val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

        init {
            loadAppointments()
        }

        private fun loadAppointments() {
            repository.currentUserId?.let { doctorId ->
                viewModelScope.launch {
                    // Сразу сортируем прилетающие данные по времени
                    repository
                        .getAppointments(doctorId)
                        .map { list ->
                            list.sortedBy { it.time }
                        }.collect {
                            _appointments.value = it
                        }
                }
            }
        }

        fun refresh() {
            viewModelScope.launch {
                _isRefreshing.value = true
                delay(500)
                loadAppointments()
                _isRefreshing.value = false
            }
        }

        fun searchPatient(iin: String) {
            if (iin.length < 12) {
                _searchPatientResult.value = null
                return
            }
            viewModelScope.launch {
                _isSearching.value = true
                _searchPatientResult.value = repository.getPatientByIin(iin)
                _isSearching.value = false
            }
        }

        fun clearSearchResult() {
            _searchPatientResult.value = null
        }

        fun addAppointment(
            patient: Patient,
            date: String,
            time: String,
        ) {
            val doctorId = repository.currentUserId ?: return
            viewModelScope.launch {
                val age =
                    try {
                        val parts = patient.birth_date.split("-")
                        val year = parts[0].toInt()
                        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                        "${currentYear - year} лет"
                    } catch (e: Exception) {
                        ""
                    }

                val appointment =
                    Appointment(
                        doctor_id = doctorId,
                        patient_iin = patient.iin,
                        patient_name = patient.full_name,
                        date = date,
                        time = time,
                        type = "", // Больше не используем
                        age = age,
                        gender = patient.gender,
                        history = "Примечание: ${patient.allergies.ifEmpty { "Нет" }}",
                        status = "Запланирован",
                        is_completed = false,
                    )
                repository.createAppointment(appointment)
            }
        }

        fun changeAppointmentStatus(
            appointment: Appointment,
            newStatus: String,
        ) {
            viewModelScope.launch {
                val isCompleted = newStatus == "Завершен" || newStatus == "Не явился"
                repository.updateAppointmentStatus(appointment.id, newStatus, isCompleted)
            }
        }
    }
