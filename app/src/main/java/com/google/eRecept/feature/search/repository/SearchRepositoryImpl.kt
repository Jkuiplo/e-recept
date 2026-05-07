package com.google.eRecept.feature.search.repository

import android.content.Context
import com.google.eRecept.data.model.Medication
import com.google.eRecept.data.model.MedicationItem
import com.google.eRecept.data.model.Patient
import com.google.eRecept.data.model.Recipe
import com.google.eRecept.data.network.api.SearchApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class SearchRepositoryImpl
    @Inject
    constructor(
        private val api: SearchApi,
        @ApplicationContext private val context: Context,
    ) : SearchRepository {
        private val prefs = context.getSharedPreferences("erecept_prefs", Context.MODE_PRIVATE)

        override val currentUserId: String?
            get() = prefs.getString("doctor_id", null)

        private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())

        override suspend fun searchPatients(query: String): List<Patient> {
            if (query.isBlank()) return emptyList()
            return try {
                val response = api.searchPatients(query)
                if (response.isSuccessful) {
                    response.body()?.map { dto ->
                        Patient(
                            iin = dto.iin,
                            full_name = dto.fullName,
                            birth_date = dto.birthDate,
                            gender = dto.gender,
                            allergies = dto.note ?: "",
                        )
                    } ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }

        override suspend fun getDoctorPatients(doctorId: String): List<Patient> =
            try {
                val response = api.getDoctorAppointments(doctorId)
                if (response.isSuccessful) {
                    response
                        .body()
                        ?.map { dto ->
                            Patient(
                                iin = dto.patientIin,
                                full_name = dto.patientFullName,
                                birth_date = dto.patientBirthDate,
                                gender = dto.patientGender,
                                allergies = dto.patientNote ?: "",
                            )
                        }?.distinctBy { it.iin } ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

        override suspend fun searchMedications(query: String): List<Medication> =
            try {
                val response = api.searchMedications(query)
                if (response.isSuccessful) {
                    response
                        .body()
                        ?.map { dto ->
                            Medication(
                                id = dto.id ?: "",
                                name = dto.name,
                                activeSubstance = dto.activeSubstance,
                                category = dto.category ?: "",
                                description = dto.description ?: "",
                                indications = dto.indications ?: "",
                                contraindications = dto.contraindications ?: "",
                                sideEffects = dto.sideEffects ?: "",
                                availableDosages = dto.availableDosages ?: emptyList(),
                                forms = dto.forms ?: emptyList(),
                            )
                        }?.sortedBy { it.name } ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

        override suspend fun getAllMedications(
            limit: Int,
            offset: Int,
        ): List<Medication> =
            try {
                val response = api.getAllMedications(limit, offset)
                if (response.isSuccessful) {
                    response
                        .body()
                        ?.map { dto ->
                            Medication(
                                id = dto.id ?: "",
                                name = dto.name,
                                activeSubstance = dto.activeSubstance,
                                category = dto.category ?: "",
                                description = dto.description ?: "",
                                indications = dto.indications ?: "",
                                contraindications = dto.contraindications ?: "",
                                sideEffects = dto.sideEffects ?: "",
                                availableDosages = dto.availableDosages ?: emptyList(),
                                forms = dto.forms ?: emptyList(),
                            )
                        }?.sortedBy { it.name } ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

        override suspend fun getRecentRecipes(doctorId: String): Flow<List<Recipe>> {
            loadRecipesFromNetwork(doctorId)
            return _recipes.asStateFlow()
        }

        private suspend fun loadRecipesFromNetwork(doctorId: String) {
            try {
                val response = api.getRecentRecipes(doctorId)
                if (response.isSuccessful) {
                    val mappedRecipes =
                        response
                            .body()
                            ?.map { dto ->
                                Recipe(
                                    id = dto.id,
                                    doctor_id = dto.doctorId,
                                    doctor_name = prefs.getString("doctor_name", "Врач") ?: "Врач",
                                    patient_iin = dto.patientIin,
                                    patient_name = dto.patientFullName ?: "Неизвестно",
                                    date = parseIsoDate(dto.createdAt),
                                    expire_date = parseIsoDate(dto.expireDate),
                                    notes = dto.notes,
                                    qr_data = dto.qrData ?: "",
                                    medications =
                                        dto.items.map { itemDto ->
                                            MedicationItem(
                                                name = itemDto.medicationName,
                                                dosageValue = itemDto.dosageValue,
                                                dosageUnit = itemDto.dosageUnit,
                                                frequency = itemDto.frequency,
                                                durationValue = itemDto.durationValue,
                                                durationUnit = itemDto.durationUnit,
                                                note = itemDto.note,
                                            )
                                        },
                                )
                            }?.sortedByDescending { it.date } ?: emptyList()

                    _recipes.value = mappedRecipes
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun parseIsoDate(dateStr: String?): Long {
            if (dateStr.isNullOrBlank()) return System.currentTimeMillis() + (10L * 24 * 60 * 60 * 1000)
            return try {
                if (dateStr.contains("T")) {
                    Instant.parse(dateStr).toEpochMilli()
                } else {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    sdf.parse(dateStr)?.time ?: System.currentTimeMillis()
                }
            } catch (e: Exception) {
                System.currentTimeMillis() + (10L * 24 * 60 * 60 * 1000)
            }
        }
    }
