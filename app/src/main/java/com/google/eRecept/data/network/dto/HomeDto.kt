package com.google.eRecept.data.network.dto

import com.google.gson.annotations.SerializedName

data class PatientDto(
    val iin: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("birth_date") val birthDate: String,
    val gender: String,
    val note: String?,
)

data class AppointmentDto(
    val id: String,
    @SerializedName("doctor_id") val doctorId: String,
    @SerializedName("patient_iin") val patientIin: String,
    @SerializedName("appointment_date") val appointmentDate: String,
    @SerializedName("appointment_time") val appointmentTime: String,
    val type: String,
    val status: String,
    val history: String?,
    @SerializedName("patient_full_name") val patientFullName: String,
    @SerializedName("patient_gender") val patientGender: String,
    @SerializedName("patient_birth_date") val patientBirthDate: String,
    @SerializedName("patient_note") val patientNote: String?,
)

data class CreateAppointmentRequest(
    @SerializedName("doctor_id") val doctorId: String,
    @SerializedName("patient_iin") val patientIin: String,
    @SerializedName("appointment_date") val appointmentDate: String,
    @SerializedName("appointment_time") val appointmentTime: String,
    val type: String,
    val status: String,
    val history: String,
)

data class DoctorProfileDto(
    val phone: String,
    val email: String,
    @SerializedName("full_name") val fullName: String,
    val specialization: String,
    @SerializedName("work_start") val workStart: String,
    @SerializedName("work_end") val workEnd: String,
    @SerializedName("break_start") val breakStart: String,
    @SerializedName("break_end") val breakEnd: String,
    @SerializedName("slot_duration") val slotDuration: Int,
)

data class UpdateStatusRequest(
    val status: String,
)
