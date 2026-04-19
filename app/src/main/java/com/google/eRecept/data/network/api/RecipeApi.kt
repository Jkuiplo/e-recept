package com.google.eRecept.data.network.api

import com.google.eRecept.data.network.dto.AppointmentDto
import com.google.eRecept.data.network.dto.CreateRecipeRequest
import com.google.eRecept.data.network.dto.MedicationDto
import com.google.eRecept.data.network.dto.RecipeResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RecipeApi {
    @GET("medications/search")
    suspend fun searchMedications(
        @Query("query") query: String,
    ): Response<List<MedicationDto>>

    @GET("recipes/{doctor_id}")
    suspend fun getRecentRecipes(
        @Path("doctor_id") doctorId: String,
    ): Response<List<RecipeResponseDto>>

    @POST("recipes")
    suspend fun createRecipe(
        @Body request: CreateRecipeRequest,
    ): Response<RecipeResponseDto>

    @GET("appointments/{doctor_id}")
    suspend fun getAppointments(
        @Path("doctor_id") doctorId: String,
        @Query("date") date: String,
    ): Response<List<AppointmentDto>>
}
