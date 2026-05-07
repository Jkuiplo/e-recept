package com.google.eRecept.feature.ai

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.eRecept.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AiMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val bitmap: Bitmap? = null
)

data class AiChatUiState(
    val messages: List<AiMessage> = emptyList(),
    val isLoading: Boolean = false,
    val selectedModel: String = "gemini-2.5-flash"
)

@HiltViewModel
class AiAssistantViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState = _uiState.asStateFlow()

    private var generativeModel = createModel(_uiState.value.selectedModel)

    private fun createModel(modelName: String) = GenerativeModel(
        modelName = modelName,
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content { text("Ты — медицинский ассистент eRecept. Помогаешь врачам анализировать рецепты и отвечать на вопросы пациентов.") }
    )

    fun updateModel(modelName: String) {
        generativeModel = createModel(modelName)
        _uiState.update { it.copy(selectedModel = modelName) }
    }

    fun sendMessage(userText: String, image: Bitmap? = null) {
        val userMsg = AiMessage(text = userText, isUser = true, bitmap = image)
        _uiState.update { it.copy(messages = it.messages + userMsg, isLoading = true) }

        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(content {
                    image?.let { image(it) }
                    text(userText)
                })

                val aiMsg = AiMessage(text = response.text ?: "Не удалось получить ответ", isUser = false)
                _uiState.update { it.copy(messages = it.messages + aiMsg, isLoading = false) }
            } catch (e: Exception) {
                // Ловим ошибку перегрузки и кривой парсинг гугла
                val errorMessage = e.message ?: ""
                val userFriendlyError = if (errorMessage.contains("503") || errorMessage.contains("MissingFieldException")) {
                    "ИИ сейчас сильно перегружен запросами 🤯. Пожалуйста, подождите минутку и попробуйте снова."
                } else {
                    "Произошла ошибка: $errorMessage"
                }

                val errorMsg = AiMessage(text = userFriendlyError, isUser = false)
                _uiState.update { it.copy(messages = it.messages + errorMsg, isLoading = false) }
            }
        }
    }

    fun createNewChat() {
        _uiState.update { it.copy(messages = emptyList()) }
    }
}