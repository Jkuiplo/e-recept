package com.google.eRecept.data.network.api

import com.google.eRecept.data.network.dto.AppointmentDto
import com.google.eRecept.data.network.dto.MedicationDto
import com.google.eRecept.data.network.dto.PatientDto
import com.google.eRecept.data.network.dto.RecipeResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SearchApi {
    @GET("patients/search")
    suspend fun searchPatients(
        @Query("query") query: String,
    ): Response<List<PatientDto>>

    @GET("medications/search")
    suspend fun searchMedications(
        @Query("query") query: String,
    ): Response<List<MedicationDto>>

    @GET("medications/")
    suspend fun getAllMedications(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): Response<List<MedicationDto>>

    @GET("recipes/{doctor_id}")
    suspend fun getRecentRecipes(
        @Path("doctor_id") doctorId: String,
    ): Response<List<RecipeResponseDto>>

    @GET("appointments/{doctor_id}")
    suspend fun getDoctorAppointments(
        @Path("doctor_id") doctorId: String,
    ): Response<List<AppointmentDto>>
}
