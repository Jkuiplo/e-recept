package com.google.eRecept.data.mockRepository

import com.google.eRecept.data.Medication
import com.google.eRecept.data.Patient
import com.google.eRecept.data.Recipe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SearchRepository {
    val currentUserId: String?

    suspend fun searchPatients(query: String): List<Patient>

    suspend fun searchMedications(query: String): List<Medication>

    fun getRecentRecipes(doctorId: String): Flow<List<Recipe>>
}

class MockSearchRepository : SearchRepository {
    override val currentUserId: String = "mock_doctor_id"

    private val mockPatients =
        listOf(
            // ALERGIES это не аллергии а примечание будет
            Patient("123456789012", "Иванов Иван Иванович", "Мужской", "1996-05-15", "Жалобы на боли в спине"),
            Patient("098765432109", "Смирнова Анна", "Женский", "2001-08-20", "Нет"),
            Patient("112233445566", "Ахметов Серик", "Мужской", "1985-11-02", "рак яичек"),
        )

    private val mockMedications =
        listOf(
            Medication(
                id = "1",
                name = "Аспирин Кардио",
                activeSubstance = "Ацетилсалициловая кислота",
                category = "Антиагрегантное средство",
                description = "Уменьшает агрегацию тромбоцитов, снижая риск тромбообразования.",
                indications = "Профилактика острого инфаркта миокарда, профилактика инсульта, нестабильная стенокардия.",
                contraindications = "Эрозивно-язвенные поражения ЖКТ, астма, почечная недостаточность, возраст до 18 лет.",
                sideEffects = "Изжога, боль в животе, микрокровотечения из ЖКТ, аллергические реакции.",
                availableDosages = listOf("100мг", "300мг"),
                forms = listOf("Таблетки, покрытые кишечнорастворимой оболочкой"),
            ),
            Medication(
                id = "2",
                name = "Амоксиклав",
                activeSubstance = "Амоксициллин + Клавулановая кислота",
                category = "Антибиотик (Пенициллины)",
                description = "Антибиотик широкого спектра действия с ингибитором бета-лактамаз.",
                indications = "Инфекции верхних и нижних дыхательных путей (бронхит, пневмония), инфекции мочевыводящих путей, инфекции кожи и мягких тканей.",
                contraindications = "Повышенная чувствительность к пенициллинам, инфекционный мононуклеоз, тяжелая печеночная недостаточность.",
                sideEffects = "Диарея, тошнота, кожная сыпь, кандидоз слизистых оболочек.",
                availableDosages = listOf("250мг+125мг", "500мг+125мг", "875мг+125мг"),
                forms = listOf("Таблетки", "Порошок для суспензии"),
            ),
            Medication(
                id = "3",
                name = "Аквадетрим",
                activeSubstance = "Колекальциферол (Витамин D3)",
                category = "Витаминный препарат",
                description = "Регулятор обмена кальция и фосфора, восполняет дефицит витамина D3.",
                indications = "Профилактика и лечение рахита, остеомаляция, поддерживающая терапия остеопороза.",
                contraindications = "Гиперкальциемия, гиперкальциурия, саркоидоз, почечная недостаточность.",
                sideEffects = "Снижение аппетита, тошнота, головная и мышечная боль (при передозировке).",
                availableDosages = listOf("15000 МЕ/мл"),
                forms = listOf("Капли для приема внутрь"),
            ),
            Medication(
                id = "4",
                name = "Нурофен Экспресс",
                activeSubstance = "Ибупрофен",
                category = "НПВС (Нестероидное противовоспалительное)",
                description = "Оказывает быстрое обезболивающее, жаропонижающее и противовоспалительное действие.",
                indications = "Головная боль, мигрень, зубная боль, невралгия, лихорадка при ОРВИ и гриппе.",
                contraindications = "Эрозивно-язвенные заболевания ЖКТ в фазе обострения, сердечная недостаточность, III триместр беременности.",
                sideEffects = "Тошнота, боль в эпигастрии, повышение АД, аллергическая сыпь.",
                availableDosages = listOf("200мг", "400мг"),
                forms = listOf("Капсулы с жидким центром"),
            ),
            Medication(
                id = "5",
                name = "Омез",
                activeSubstance = "Омепразол",
                category = "Ингибитор протонной помпы (ЖКТ)",
                description = "Снижает секрецию желез желудка, уменьшая кислотность.",
                indications = "Язвенная болезнь желудка и двенадцатиперстной кишки, ГЭРБ (рефлюкс-эзофагит).",
                contraindications = "Повышенная чувствительность к препарату, детский возраст (зависит от формы), совместный прием с нелфинавиром.",
                sideEffects = "Головная боль, диарея, запор, боль в животе, метеоризм.",
                availableDosages = listOf("10мг", "20мг", "40мг"),
                forms = listOf("Капсулы кишечнорастворимые", "Лиофилизат для раствора"),
            ),
        )

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
