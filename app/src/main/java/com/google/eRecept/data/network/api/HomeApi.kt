package com.google.eRecept.data.network.api

import com.google.eRecept.data.network.dto.AppointmentDto
import com.google.eRecept.data.network.dto.CreateAppointmentRequest
import com.google.eRecept.data.network.dto.DoctorProfileDto
import com.google.eRecept.data.network.dto.PatientDto
import com.google.eRecept.data.network.dto.UpdateStatusRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HomeApi {
    @GET("patients/search")
    suspend fun searchPatients(
        @Query("query") query: String,
    ): Response<List<PatientDto>>

    @GET("appointments/{doctor_id}")
    suspend fun getAppointments(
        @Path("doctor_id") doctorId: String,
        @Query("date") date: String,
    ): Response<List<AppointmentDto>>

    @POST("appointments")
    suspend fun createAppointment(
        @Body request: CreateAppointmentRequest,
    ): Response<AppointmentDto>

    @PATCH("appointments/{id}/status")
    suspend fun updateAppointmentStatus(
        @Path("id") id: String,
        @Body request: UpdateStatusRequest,
    ): Response<Unit>

    @GET("profile/{doctor_id}")
    suspend fun getDoctorProfile(
        @Path("doctor_id") doctorId: String,
    ): Response<DoctorProfileDto>
}
