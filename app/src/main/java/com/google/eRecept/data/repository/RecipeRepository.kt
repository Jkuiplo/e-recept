package com.google.eRecept.data.repository

import com.google.eRecept.data.Doctor
import com.google.eRecept.data.Medication
import com.google.eRecept.data.Recipe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface RecipeRepository {
    val currentUserId: String?

    fun getRecentRecipes(doctorId: String): Flow<List<Recipe>>

    suspend fun searchMedications(query: String): List<Medication>

    suspend fun getDoctorProfile(doctorId: String): Doctor?

    suspend fun createRecipe(recipe: Recipe)
}

class MockRecipeRepository : RecipeRepository {
    override val currentUserId: String = "mock_doctor_id"

    // Реактивный список, который будет обновляться при добавлении
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())

    // Наша локальная "база" препаратов для теста автопредложений
    private val mockMedications =
        listOf(
            Medication("1", "Аспирин", "Ацетилсалициловая кислота", "НПВС", "", listOf("100мг", "500мг")),
            Medication("2", "Амоксициллин", "Амоксициллин", "Антибиотик", "", listOf("250мг", "500мг", "1000мг")),
            Medication("3", "Аквадетрим", "Колекальциферол", "Витамин", "", listOf("15000 МЕ/мл")),
            Medication("4", "Ибупрофен", "Ибупрофен", "НПВС", "", listOf("200мг", "400мг")),
            Medication("5", "Парацетамол", "Парацетамол", "Анальгетик", "", listOf("500мг")),
        )

    override fun getRecentRecipes(doctorId: String): Flow<List<Recipe>> = _recipes.asStateFlow()

    override suspend fun searchMedications(query: String): List<Medication> {
        delay(400) // Имитация загрузки для скелетона
        if (query.isBlank()) return mockMedications // Возвращаем всё!

        return mockMedications.filter {
            it.name.contains(query, ignoreCase = true) || it.activeSubstance.contains(query, ignoreCase = true)
        }
    }

    override suspend fun getDoctorProfile(doctorId: String): Doctor? = Doctor("mock_doctor_id", "Д-р Хаус", "Терапевт")

    override suspend fun createRecipe(recipe: Recipe) {
        delay(500)
        val currentList = _recipes.value.toMutableList()
        // Добавляем новый рецепт в начало списка и генерируем ему фейковый ID
        currentList.add(0, recipe.copy(id = "REC-${System.currentTimeMillis()}"))
        _recipes.value = currentList
    }
}
