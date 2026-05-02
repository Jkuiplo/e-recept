package com.google.eRecept.feature.recipe.repository

import com.google.eRecept.data.model.Doctor
import com.google.eRecept.data.model.Medication
import com.google.eRecept.data.model.Recipe
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

    suspend fun getAllMedications(
        limit: Int = 500,
        offset: Int = 0,
    ): List<Medication>

    suspend fun getDoctorProfile(doctorId: String): Doctor?

    suspend fun createRecipe(recipe: Recipe)
}
