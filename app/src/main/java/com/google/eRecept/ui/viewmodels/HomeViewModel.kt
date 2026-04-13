package com.google.eRecept.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.data.Appointment
import com.google.eRecept.data.FirebaseRepository
import com.google.eRecept.data.Patient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(private val repository: FirebaseRepository = FirebaseRepository()) : ViewModel() {
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _searchPatientResult = MutableStateFlow<Patient?>(null)
    val searchPatientResult: StateFlow<Patient?> = _searchPatientResult.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        repository.currentUserId?.let { doctorId ->
            viewModelScope.launch {
                repository.getAppointments(doctorId).collect {
                    _appointments.value = it
                }
            }
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

    fun addAppointment(patient: Patient, date: String, time: String, type: String) {
        val doctorId = repository.currentUserId ?: return
        viewModelScope.launch {
            // Calculate age from birth_date (yyyy-mm-dd)
            val age = try {
                val parts = patient.birth_date.split("-")
                val year = parts[0].toInt()
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                (currentYear - year).toString() + " лет"
            } catch (e: Exception) {
                ""
            }

            val appointment = Appointment(
                doctor_id = doctorId,
                patient_iin = patient.iin,
                patient_name = patient.full_name,
                date = date,
                time = time,
                type = type,
                age = age,
                gender = patient.gender,
                history = "Аллергии: ${patient.allergies}"
            )
            repository.createAppointment(appointment)
        }
    }
}
