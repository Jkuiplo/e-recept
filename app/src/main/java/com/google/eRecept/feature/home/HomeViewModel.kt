package com.google.eRecept.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.data.model.Appointment
import com.google.eRecept.data.model.AppointmentStatus
import com.google.eRecept.data.model.Patient
import com.google.eRecept.data.model.DoctorSchedule
import com.google.eRecept.feature.home.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        private val _doctorSchedule = MutableStateFlow<DoctorSchedule?>(null)
        val doctorSchedule: StateFlow<DoctorSchedule?> = _doctorSchedule.asStateFlow()

        init {
            loadAppointments()
        }

        private fun loadAppointments() {
            repository.currentUserId?.let { doctorId ->
                viewModelScope.launch {
                    _isLoading.value = true
                    _doctorSchedule.value = repository.getDoctorSchedule(doctorId)

                    repository
                        .getAppointments(doctorId)
                        .map { list -> list.sortedBy { it.time } }
                        .collect { 
                            _appointments.value = it 
                            _isLoading.value = false
                        }
                }
            }
        }

        fun getAvailableTimeSlots(date: String): List<String> {
            val schedule = _doctorSchedule.value ?: return emptyList()

            val bookedTimes =
                _appointments.value
                    .filter { it.date == date && AppointmentStatus.fromBackendString(it.status) != AppointmentStatus.CANCELLED }
                    .map { it.time }

            val slots = mutableListOf<String>()

            val start = parseTime(schedule.workStart)
            val end = parseTime(schedule.workEnd)
            val breakStart = parseTime(schedule.breakStart)
            val breakEnd = parseTime(schedule.breakEnd)

            val current = start.clone() as Calendar

            val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val now = Date()

            while (current.before(end)) {
                val slotUiStr = String.format("%02d:%02d", current.get(Calendar.HOUR_OF_DAY), current.get(
                    Calendar.MINUTE))

                val isDuringBreak = current.timeInMillis >= breakStart.timeInMillis && current.timeInMillis < breakEnd.timeInMillis

                val isPast =
                    try {
                        val slotDateTime = dateTimeFormat.parse("$date $slotUiStr")
                        slotDateTime != null && slotDateTime.before(now)
                    } catch (e: Exception) {
                        false
                    }

                if (!isDuringBreak && !bookedTimes.contains(slotUiStr) && !isPast) {
                    slots.add(slotUiStr)
                }

                current.add(Calendar.MINUTE, schedule.slotDuration)
            }

            return slots
        }

        fun refresh() {
            viewModelScope.launch {
                _isRefreshing.value = true
                try {
                    repository.currentUserId?.let { doctorId ->
                        _doctorSchedule.value = repository.getDoctorSchedule(doctorId)
                        val list = repository.getAppointments(doctorId).first()
                        _appointments.value = list.sortedBy { it.time }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _isRefreshing.value = false
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
                        type = "",
                        age = age,
                        gender = patient.gender,
                        history = patient.allergies,
                        status = AppointmentStatus.PLANNED.backendValue,
                        is_completed = false,
                    )
                repository.createAppointment(appointment)
            }
        }

        fun changeAppointmentStatus(
            appointment: Appointment,
            newStatus: AppointmentStatus,
        ) {
            viewModelScope.launch {
                val isCompleted = newStatus == AppointmentStatus.COMPLETED || newStatus == AppointmentStatus.NO_SHOW
                repository.updateAppointmentStatus(appointment.id, newStatus.backendValue, isCompleted)
            }
        }

        private fun parseTime(timeStr: String): Calendar {
            val cal =
                Calendar.getInstance().apply {
                    set(Calendar.YEAR, 2000)
                    set(Calendar.MONTH, 0)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
            val parts = timeStr.split(":")
            cal.set(Calendar.HOUR_OF_DAY, parts.getOrNull(0)?.toInt() ?: 0)
            cal.set(Calendar.MINUTE, parts.getOrNull(1)?.toInt() ?: 0)
            cal.set(Calendar.SECOND, parts.getOrNull(2)?.toInt() ?: 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal
        }
    }