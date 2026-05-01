package com.google.eRecept.data.model

data class Doctor(
    val id: String = "",
    val name: String = "",
    val specialization: String = "",
)

data class Patient(
    val iin: String = "",
    val full_name: String = "",
    val gender: String = "",
    val birth_date: String = "",
    val allergies: String = "",
)

data class Appointment(
    val id: String = "",
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
    val gender: String = "",
)

data class Recipe(
    val id: String = "",
    val status: String = "Активен",
    val doctor_id: String = "",
    val doctor_name: String = "",
    val patient_iin: String = "",
    val patient_name: String = "",
    val date: Long = System.currentTimeMillis(),
    val expire_date: Long = System.currentTimeMillis() + (10L * 24 * 60 * 60 * 1000),
    val medications: List<MedicationItem> = emptyList(),
    val notes: String = "",
    val qr_data: String = "",
) {
    val isActive: Boolean
        get() = System.currentTimeMillis() <= expire_date
}

data class MedicationItem(
    val id: String = "",
    val name: String = "",
    val dosageValue: String = "1",
    val dosageUnit: String = "мг",
    val frequency: String = "2×",
    val durationValue: String = "1",
    val durationUnit: String = "дней",
    val note: String = "",
) {
    val summary: String
        get() =
            if (dosageValue.isNotBlank() && durationValue.isNotBlank()) {
                "$dosageValue $dosageUnit × $frequency/день — $durationValue $durationUnit"
            } else {
                "Заполните дозировку и длительность"
            }
}

data class Medication(
    val id: String = "",
    val name: String = "",
    val activeSubstance: String = "",
    val category: String = "",
    val description: String = "",
    val indications: String = "",
    val contraindications: String = "",
    val sideEffects: String = "", // Убрать надо бы
    val availableDosages: List<String> = emptyList(),
    val forms: List<String> = emptyList(),
)

data class DoctorSchedule(
    val workStart: String,
    val workEnd: String,
    val breakStart: String,
    val breakEnd: String,
    val slotDuration: Int,
)