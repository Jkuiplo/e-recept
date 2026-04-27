package com.google.eRecept.data.mockRepository

import com.google.eRecept.data.Doctor
import com.google.eRecept.data.Medication
import com.google.eRecept.data.Recipe
import com.google.eRecept.data.network.dto.UpdateRecipeRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface RecipeRepository {
    val currentUserId: String?

    suspend fun revokeRecipe(recipeId: String): Boolean

    suspend fun updateRecipe(
        recipeId: String,
        request: UpdateRecipeRequest,
    ): Boolean

    suspend fun getRecentRecipes(doctorId: String): Flow<List<Recipe>>

    suspend fun searchMedications(query: String): List<Medication>

    suspend fun getDoctorProfile(doctorId: String): Doctor?

    suspend fun createRecipe(recipe: Recipe)
}

class MockRecipeRepository : RecipeRepository {
    override val currentUserId: String = "mock_doctor_id"

    override suspend fun revokeRecipe(recipeId: String): Boolean {
        kotlinx.coroutines.delay(500)
        val currentList = _recipes.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == recipeId }

        if (index != -1) {
            val revokedRecipe =
                currentList[index].copy(
                    expire_date = System.currentTimeMillis() - 1000,
                    status = "Отозван",
                )
            currentList[index] = revokedRecipe
            _recipes.value = currentList
            return true
        }
        return false
    }

    override suspend fun updateRecipe(
        recipeId: String,
        request: UpdateRecipeRequest,
    ): Boolean {
        TODO("Not yet implemented")
    }

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())

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
            // ... остальные мок-медикаменты
        )

    override suspend fun getRecentRecipes(doctorId: String): Flow<List<Recipe>> = _recipes.asStateFlow()

    override suspend fun searchMedications(query: String): List<Medication> {
        delay(400)
        if (query.isBlank()) return mockMedications

        return mockMedications.filter {
            it.name.contains(query, ignoreCase = true) || it.activeSubstance.contains(query, ignoreCase = true)
        }
    }

    override suspend fun getDoctorProfile(doctorId: String): Doctor? =
        Doctor("mock_doctor_id", "Иванов Иван Иванович", "Врач-терапевт, Кардиолог")

    override suspend fun createRecipe(recipe: Recipe) {
        delay(500)
        val currentList = _recipes.value.toMutableList()
        currentList.add(0, recipe.copy(id = "REC-${System.currentTimeMillis()}"))
        _recipes.value = currentList
    }
}
