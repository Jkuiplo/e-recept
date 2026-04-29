package com.google.eRecept.feature.home.repository

import com.google.eRecept.data.model.Appointment
import com.google.eRecept.data.model.Patient
import kotlinx.coroutines.flow.Flow
import com.google.eRecept.data.model.DoctorSchedule

interface HomeRepository {
    val currentUserId: String?

    suspend fun getDoctorSchedule(doctorId: String): DoctorSchedule?

    suspend fun getAppointments(doctorId: String): Flow<List<Appointment>>

    suspend fun getPatientByIin(iin: String): Patient?

    suspend fun createAppointment(appointment: Appointment)

    suspend fun updateAppointmentStatus(
        appointmentId: String,
        newStatus: String,
        isCompleted: Boolean,
    )
}