package com.google.eRecept.feature.experimental

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.core.utils.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExperimentalFeaturesViewModel @Inject constructor(
    private val dataStore: SettingsDataStore // Hilt сам передаст сюда объект благодаря AppModule!
) : ViewModel() {

    // Сразу отдаем Flow в UI, ViewModel даже не нужно хранить свой стейт
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
}