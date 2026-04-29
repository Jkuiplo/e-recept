package com.google.eRecept.feature.search.repository

import com.google.eRecept.data.model.Medication
import com.google.eRecept.data.model.Patient
import com.google.eRecept.data.model.Recipe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SearchRepository {
    val currentUserId: String?

    suspend fun searchPatients(query: String): List<Patient>

    suspend fun searchMedications(query: String): List<Medication>

    suspend fun getRecentRecipes(doctorId: String): Flow<List<Recipe>>

    suspend fun getDoctorPatients(doctorId: String): List<Patient>

    suspend fun getAllMedications(
        limit: Int,
        offset: Int,
    ): List<Medication>
}