package com.google.eRecept.data

import com.google.firebase.firestore.DocumentId

data class Doctor(
    @DocumentId val id: String = "",
    val name: String = "",
    val specialization: String = ""
)

data class Patient(
    @DocumentId val iin: String = "",
    val full_name: String = "",
    val gender: String = "",
    val birth_date: String = "",
    val allergies: String = ""
)

data class Appointment(
    @DocumentId val id: String = "", // Firestore ID
    val doctor_id: String = "",
    val patient_iin: String = "",
    val patient_name: String = "",
    val date: String = "",
    val time: String = "",
    val type: String = "Первичный",
    val status: String = "Запланирована",
    val is_completed: Boolean = false,
    val history: String = "",
    val age: String = "",
    val gender: String = ""
)

data class Recipe(
    @DocumentId val id: String = "",
    val doctor_id: String = "",
    val doctor_name: String = "",
    val patient_iin: String = "",
    val patient_name: String = "",
    val date: Long = System.currentTimeMillis(),
    val medications: List<MedicationItem> = emptyList(),
    val notes: String = ""
)

data class MedicationItem(
    val name: String = "",
    val dosage: String = ""
)

data class Medication(
    @DocumentId val id: String = "",
    val name: String = "",            // Коммерческое название
    val activeSubstance: String = "", // МНН
    val category: String = "",        // Категория
    val description: String = "",
    val availableDosages: List<String> = emptyList(),
    val forms: List<String> = emptyList()
)
