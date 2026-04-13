package com.google.eRecept.data

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
    val doctor_id: String = "",
    val doctor_name: String = "",
    val patient_iin: String = "",
    val patient_name: String = "",
    val date: Long = System.currentTimeMillis(),
    val medications: List<MedicationItem> = emptyList(),
    val notes: String = "",
)

data class MedicationItem(
    val name: String = "",
    val dosageValue: String = "",
    val dosageUnit: String = "мг",
    val frequency: String = "2×",
    val durationValue: String = "",
    val durationUnit: String = "дней",
) {
    // Автоматически собираем строку "Итого"
    val summary: String
        get() =
            if (dosageValue.isNotBlank() && durationValue.isNotBlank()) {
                "$dosageValue $dosageUnit × $frequency/день — $durationValue $durationUnit"
            } else {
                "Заполните данные"
            }
}

data class Medication(
    val id: String = "",
    val name: String = "", // Коммерческое название
    val activeSubstance: String = "", // МНН
    val category: String = "", // Категория
    val description: String = "",
    val availableDosages: List<String> = emptyList(),
    val forms: List<String> = emptyList(),
)
