package com.google.eRecept.data.repository

import com.google.eRecept.data.Medication
import com.google.eRecept.data.Patient
import com.google.eRecept.data.Recipe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Контракт
interface SearchRepository {
    val currentUserId: String?

    suspend fun searchPatients(query: String): List<Patient>

    suspend fun searchMedications(query: String): List<Medication>

    fun getRecentRecipes(doctorId: String): Flow<List<Recipe>>
}

// Заглушка
class MockSearchRepository : SearchRepository {
    override val currentUserId: String = "mock_doctor_id"

    // Мок-база пациентов
    private val mockPatients =
        listOf(
            // ALERGIES это не аллергии а примечание будет
            Patient("123456789012", "Иванов Иван Иванович", "Мужской", "1996-05-15", "Жалобы на боли в спине"),
            Patient("098765432109", "Смирнова Анна", "Женский", "2001-08-20", "Нет"),
            Patient("112233445566", "Ахметов Серик", "Мужской", "1985-11-02", "рак яичек"),
        )

    // Мок-база препаратов (добавил описания, чтобы на экране инфы красиво смотрелось)
    private val mockMedications =
        listOf(
            Medication(
                "1",
                "Аспирин",
                "Ацетилсалициловая кислота",
                "НПВС",
                "Применяется как обезболивающее и жаропонижающее средство.",
                listOf("100мг", "500мг"),
            ),
            Medication(
                "2",
                "Амоксициллин",
                "Амоксициллин",
                "Антибиотик",
                "Антибиотик широкого спектра действия.",
                listOf("250мг", "500мг", "1000мг"),
            ),
            Medication("3", "Аквадетрим", "Колекальциферол", "Витамин", "Восполняет дефицит витамина D3.", listOf("15000 МЕ/мл")),
            Medication("4", "Ибупрофен", "Ибупрофен", "НПВС", "Оказывает противовоспалительное действие.", listOf("200мг", "400мг")),
            Medication("5", "Парацетамол", "Парацетамол", "Анальгетик", "Снимает жар и легкую боль.", listOf("500мг")),
        )

    // Фейковая история рецептов
    private val _recipes =
        MutableStateFlow<List<Recipe>>(
            listOf(
                Recipe(
                    id = "REC-987654321",
                    doctor_id = currentUserId,
                    doctor_name = "Д-р Хаус",
                    patient_iin = "123456789012",
                    patient_name = "Иванов Иван Иванович",
                    date = System.currentTimeMillis() - 86400000, // Вчера
                    medications = emptyList(),
                    notes = "Соблюдать постельный режим",
                ),
                Recipe(
                    id = "REC-123456789",
                    doctor_id = currentUserId,
                    doctor_name = "Д-р Хаус",
                    patient_iin = "098765432109",
                    patient_name = "Смирнова Анна",
                    date = System.currentTimeMillis(), // Сегодня
                    medications = emptyList(),
                    notes = "Пить больше воды",
                ),
            ),
        )

    override suspend fun searchPatients(query: String): List<Patient> {
        delay(400) // Лоадер покрутится 0.4 сек
        return mockPatients.filter {
            it.iin.contains(query) || it.full_name.contains(query, ignoreCase = true)
        }
    }

    override suspend fun searchMedications(query: String): List<Medication> {
        delay(400) // Лоадер покрутится 0.4 сек
        return mockMedications.filter {
            it.name.contains(query, ignoreCase = true) || it.activeSubstance.contains(query, ignoreCase = true)
        }
    }

    override fun getRecentRecipes(doctorId: String): Flow<List<Recipe>> = _recipes.asStateFlow()
}
