package com.google.eRecept.core.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "experimental_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val VOICE_RECIPE_ENABLED = booleanPreferencesKey("voice_recipe_enabled")
        val AI_ASSISTANT_ENABLED = booleanPreferencesKey("ai_assistant_enabled")
        val THEME_PALETTE_SWITCHER = booleanPreferencesKey("theme_palette_switcher")
    }

    val isVoiceRecipeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[VOICE_RECIPE_ENABLED] ?: false }

    val isAiAssistantEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[AI_ASSISTANT_ENABLED] ?: false }

    suspend fun toggleVoiceRecipe(enabled: Boolean) {
        context.dataStore.edit { it[VOICE_RECIPE_ENABLED] = enabled }
    }

    suspend fun toggleAiAssistant(enabled: Boolean) {
        context.dataStore.edit { it[AI_ASSISTANT_ENABLED] = enabled }
    }
}