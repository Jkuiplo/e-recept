package com.google.eRecept.data.repository

import android.content.Context
import com.google.eRecept.data.Appointment
import com.google.eRecept.data.Patient
import com.google.eRecept.data.mockRepository.DoctorSchedule
import com.google.eRecept.data.mockRepository.HomeRepository
import com.google.eRecept.data.network.api.HomeApi
import com.google.eRecept.data.network.dto.CreateAppointmentRequest
import com.google.eRecept.data.network.dto.UpdateStatusRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class NetworkHomeRepository
    @Inject
    constructor(
        private val api: HomeApi,
        @ApplicationContext private val context: Context,
    ) : HomeRepository {
        override val currentUserId: String?
            get() = context.getSharedPreferences("erecept_prefs", Context.MODE_PRIVATE).getString("doctor_id", null)

        @Suppress("ktlint:standard:backing-property-naming")
        private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())

        override suspend fun getAppointments(doctorId: String): Flow<List<Appointment>> {
            loadAppointmentsForThreeDays(doctorId)
            return _appointments.asStateFlow()
        }

        private suspend fun loadAppointmentsForThreeDays(doctorId: String) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val uiSdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

            val datesForApi = mutableListOf<String>()
            val datesForUi = mutableListOf<String>()

            val cal = Calendar.getInstance()
            for (i in 0..2) {
                datesForApi.add(sdf.format(cal.time))
                datesForUi.add(uiSdf.format(cal.time))
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }

            try {
                coroutineScope {
                    val deferredResults =
                        datesForApi.mapIndexed { index, dateStr ->
                            async {
                                val response = api.getAppointments(doctorId, dateStr)
                                if (response.isSuccessful) {
                                    response.body()?.map { dto ->
                                        Appointment(
                                            id = dto.id,
                                            doctor_id = dto.doctorId,
                                            patient_iin = dto.patientIin,
                                            patient_name = dto.patientFullName,
                                            date = datesForUi[index],
                                            time = dto.appointmentTime,
                                            type = dto.type,
                                            status = dto.status,
                                            history = dto.history ?: "",
                                            age = calculateAge(dto.patientBirthDate),
                                            gender = dto.patientGender,
                                            is_completed =
                                                dto.status == "Состоялась" || dto.status == "Не явился" || dto.status == "Отменена",
                                        )
                                    } ?: emptyList()
                                } else {
                                    emptyList()
                                }
                            }
                        }

                    val allAppointments = deferredResults.awaitAll().flatten()
                    _appointments.value = allAppointments
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override suspend fun getPatientByIin(iin: String): Patient? =
            try {
                val response = api.searchPatients(iin)
                if (response.isSuccessful) {
                    val dto = response.body()?.firstOrNull { it.iin == iin }
                    dto?.let {
                        Patient(
                            iin = it.iin,
                            full_name = it.fullName,
                            birth_date = it.birthDate,
                            gender = it.gender,
                            allergies = it.note ?: "",
                        )
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }

        override suspend fun createAppointment(appointment: Appointment) {
            val request =
                CreateAppointmentRequest(
                    doctorId = appointment.doctor_id,
                    patientIin = appointment.patient_iin,
                    appointmentDate = formatToApiDate(appointment.date),
                    appointmentTime = appointment.time,
                    type = "Первичный",
                    status = "Запланирована",
                    history = appointment.history,
                )

            try {
                val response = api.createAppointment(request)
                if (response.isSuccessful) {
                    currentUserId?.let { loadAppointmentsForThreeDays(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override suspend fun updateAppointmentStatus(
            appointmentId: String,
            newStatus: String,
            isCompleted: Boolean,
        ) {
            try {
                val response = api.updateAppointmentStatus(appointmentId, UpdateStatusRequest(newStatus))
                if (response.isSuccessful) {
                    val currentList = _appointments.value.toMutableList()
                    val index = currentList.indexOfFirst { it.id == appointmentId }
                    if (index != -1) {
                        currentList[index] = currentList[index].copy(status = newStatus, is_completed = isCompleted)
                        _appointments.value = currentList
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override suspend fun getDoctorSchedule(doctorId: String): DoctorSchedule? =
            try {
                val response = api.getDoctorProfile(doctorId)
                if (response.isSuccessful) {
                    response.body()?.let { dto ->
                        DoctorSchedule(
                            workStart = dto.workStart,
                            workEnd = dto.workEnd,
                            breakStart = dto.breakStart,
                            breakEnd = dto.breakEnd,
                            slotDuration = dto.slotDuration,
                        )
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

        private fun calculateAge(birthDate: String): String =
            try {
                val year = birthDate.split("-")[0].toInt()
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                "${currentYear - year} лет"
            } catch (e: Exception) {
                ""
            }

        private fun formatToApiDate(uiDate: String): String =
            try {
                val uiFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = uiFormat.parse(uiDate)
                apiFormat.format(date!!)
            } catch (e: Exception) {
                uiDate
            }
    }
