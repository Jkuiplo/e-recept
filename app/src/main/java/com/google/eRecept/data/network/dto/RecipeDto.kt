package com.google.eRecept.data.network.dto

import com.google.gson.annotations.SerializedName

data class MedicationDto(
    val id: String?,
    val name: String,
    @SerializedName("active_substance") val activeSubstance: String,
    val category: String?,
    val description: String?,
    val indications: String?,
    val contraindications: String?,
    @SerializedName("side_effects") val sideEffects: String?,
    @SerializedName("available_dosages") val availableDosages: List<String>?,
    val forms: List<String>?,
)

data class RecipeItemDto(
    @SerializedName("medication_name") val medicationName: String,
    @SerializedName("dosage_value") val dosageValue: String,
    @SerializedName("dosage_unit") val dosageUnit: String,
    val frequency: String,
    @SerializedName("duration_value") val durationValue: String,
    @SerializedName("duration_unit") val durationUnit: String,
    val note: String,
)

data class CreateRecipeRequest(
    @SerializedName("doctor_id") val doctorId: String,
    @SerializedName("patient_iin") val patientIin: String,
    @SerializedName("expire_days") val expireDays: Int,
    val notes: String,
    val items: List<RecipeItemDto>,
)

data class RecipeResponseDto(
    val id: String,
    @SerializedName("doctor_id") val doctorId: String,
    @SerializedName("patient_iin") val patientIin: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("expire_date") val expireDate: String,
    val notes: String,
    @SerializedName("patient_full_name") val patientFullName: String?,
    @SerializedName("qr_data") val qrData: String?,
    val items: List<RecipeItemDto>,
)
