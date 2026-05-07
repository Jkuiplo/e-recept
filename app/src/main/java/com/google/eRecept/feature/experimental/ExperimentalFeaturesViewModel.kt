package com.google.eRecept.feature.experimental

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.core.utils.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExperimentalFeaturesViewModel @Inject constructor(
    private val dataStore: SettingsDataStore,
    application: Application
) : AndroidViewModel(application) {

    val isVoiceEnabled = dataStore.isVoiceRecipeEnabled
    val isAiEnabled = dataStore.isAiAssistantEnabled

    fun toggleVoice(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.toggleVoiceRecipe(enabled)
        }
    }

    fun toggleAi(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.toggleAiAssistant(enabled)
        }
    }

    private val prefs = application.getSharedPreferences("erecept_prefs", Context.MODE_PRIVATE)
    private val _language = MutableStateFlow(prefs.getString("language", "ru") ?: "ru")

    fun updateLanguage(tag: String) {
        prefs.edit { putString("language", tag) }
        _language.value = tag
    }
}