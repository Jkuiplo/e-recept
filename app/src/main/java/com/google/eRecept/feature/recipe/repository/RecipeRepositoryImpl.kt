package com.google.eRecept.feature.recipe.repository

import android.content.Context
import com.google.eRecept.data.model.Doctor
import com.google.eRecept.data.model.Medication
import com.google.eRecept.data.model.MedicationItem
import com.google.eRecept.data.model.Recipe
import com.google.eRecept.data.network.api.RecipeApi
import com.google.eRecept.data.network.dto.CreateRecipeRequest
import com.google.eRecept.data.network.dto.RecipeItemDto
import com.google.eRecept.data.network.dto.UpdateRecipeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class RecipeRepositoryImpl
    @Inject
    constructor(
        private val api: RecipeApi,
        @ApplicationContext private val context: Context,
    ) : RecipeRepository {
        private val prefs = context.getSharedPreferences("erecept_prefs", Context.MODE_PRIVATE)

        override val currentUserId: String?
            get() = prefs.getString("doctor_id", null)

        @Suppress("ktlint:standard:backing-property-naming")
        private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())

        override suspend fun getRecentRecipes(doctorId: String): Flow<List<Recipe>> {
            loadRecipesFromNetwork(doctorId)
            return _recipes.asStateFlow()
        }

        private suspend fun loadRecipesFromNetwork(doctorId: String) {
            try {
                val response = api.getRecentRecipes(doctorId)
                if (response.isSuccessful) {
                    val mappedRecipes =
                        response.body()?.map { dto ->
                            Recipe(
                                id = dto.id,
                                doctor_id = dto.doctorId,
                                doctor_name = getDoctorProfile(dto.doctorId)?.name ?: "Врач",
                                patient_iin = dto.patientIin,
                                patient_name = dto.patientFullName ?: "Неизвестно",
                                date = parseIsoDate(dto.createdAt),
                                expire_date = parseIsoDate(dto.expireDate),
                                notes = dto.notes,
                                status = dto.status ?: "Активен",
                                qr_data = dto.qrData ?: "",
                                medications =
                                    dto.items.map { itemDto ->
                                        MedicationItem(
                                            id = itemDto.medicationId ?: "",
                                            name = itemDto.medicationName,
                                            dosageValue = itemDto.dosageValue,
                                            dosageUnit = itemDto.dosageUnit,
                                            frequency = if (itemDto.frequency.contains("×")) itemDto.frequency else "${itemDto.frequency}×",
                                            durationValue = itemDto.durationValue,
                                            durationUnit = itemDto.durationUnit,
                                            note = itemDto.note,
                                        )
                                    },
                            )
                        } ?: emptyList()

                    _recipes.value = mappedRecipes
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override suspend fun searchMedications(query: String): List<Medication> =
            try {
                val response = api.searchMedications(query)
                if (response.isSuccessful) {
                    response.body()?.map { dto ->
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
                    } ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
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

        override suspend fun getDoctorProfile(doctorId: String): Doctor? {
            val docName = prefs.getString("doctor_name", "Иванов Иван Иванович")
            return Doctor(id = doctorId, name = docName!!, specialization = "Врач")
        }

        override suspend fun createRecipe(recipe: Recipe) {
            val diffMillis = recipe.expire_date - recipe.date
            val days = (diffMillis / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)

            val request =
                CreateRecipeRequest(
                    doctorId = recipe.doctor_id,
                    patientIin = recipe.patient_iin,
                    expireDays = days,
                    notes = recipe.notes,
                    items =
                        recipe.medications.map {
                            RecipeItemDto(
                                medicationId = it.id.ifBlank { null },
                                medicationName = it.name,
                                dosageValue = it.dosageValue,
                                dosageUnit = it.dosageUnit,
                                frequency = it.frequency,
                                durationValue = it.durationValue,
                                durationUnit = it.durationUnit,
                                note = it.note,
                            )
                        },
                )

            try {
                val response = api.createRecipe(request)
                if (response.isSuccessful) {
                    currentUserId?.let { loadRecipesFromNetwork(it) }
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
                e.printStackTrace()
                System.currentTimeMillis() + (10L * 24 * 60 * 60 * 1000)
            }
        }

        override suspend fun revokeRecipe(recipeId: String): Boolean =
            try {
                val response = api.revokeRecipe(recipeId)
                if (response.isSuccessful) {
                    println("E-RECEPT: Успешно отозвано!")

                    val updatedDto = response.body()
                    val currentList = _recipes.value.toMutableList()
                    val index = currentList.indexOfFirst { it.id == recipeId }

                    if (index != -1) {
                        val oldRecipe = currentList[index]
                        val newStatus = updatedDto?.status ?: "Отозван"

                        val revokedRecipe =
                            oldRecipe.copy(
                                status = newStatus,
                                expire_date = if (newStatus != "Активен") System.currentTimeMillis() - 1000 else oldRecipe.expire_date,
                            )
                        currentList[index] = revokedRecipe
                        _recipes.value = currentList
                    } else {
                        currentUserId?.let { loadRecipesFromNetwork(it) }
                    }
                    true
                } else {
                    println("E-RECEPT ОШИБКА: Код ${response.code()}, Тело: ${response.errorBody()?.string()}")
                    false
                }
            } catch (e: Exception) {
                println("E-RECEPT КРАШ: ${e.message}")
                e.printStackTrace()
                false
            }

        override suspend fun updateRecipe(
            recipeId: String,
            request: UpdateRecipeRequest,
        ): Boolean =
            try {
                val response = api.updateRecipe(recipeId, request)
                if (response.isSuccessful) {
                    currentUserId?.let { loadRecipesFromNetwork(it) }
                    true
                } else {
                    println("E-RECEPT UPDATE ERROR: ${response.errorBody()?.string()}")
                    false
                }
            } catch (e: Exception) {
                println("E-RECEPT UPDATE ERROR (CATCH): ${e.message}")
                e.printStackTrace()
                false
            }
        override suspend fun parseVoiceRecipe(text: String): com.google.eRecept.data.network.dto.AiScribeResponse? =
            try {
                val response = api.parseVoice(com.google.eRecept.data.network.dto.AiScribeRequest(text))
                if (response.isSuccessful) {
                    response.body()
                } else {
                    println("E-RECEPT AI ERROR: ${response.errorBody()?.string()}")
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
    }