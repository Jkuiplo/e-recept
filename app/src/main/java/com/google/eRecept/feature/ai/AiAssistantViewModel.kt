package com.google.eRecept.feature.ai

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.eRecept.BuildConfig
import com.google.eRecept.data.local.entity.ChatEntity
import com.google.eRecept.data.local.entity.MessageEntity
import com.google.eRecept.feature.ai.repository.AiChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
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
    val currentChatId: String? = null,
    val messages: List<AiMessage> = emptyList(),
    val chatHistory: List<ChatEntity> = emptyList(),
    val isLoading: Boolean = false,
    val selectedModel: String = "gemini-2.5-flash"
)

@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val repository: AiChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState = _uiState.asStateFlow()

    private var messagesJob: Job? = null
    private var generativeModel = createModel(_uiState.value.selectedModel)

    init {
        viewModelScope.launch {
            repository.getAllChats().collect { chats ->
                _uiState.update { it.copy(chatHistory = chats) }
            }
        }
        createNewChat()
    }

    private fun createModel(modelName: String) = GenerativeModel(
        modelName = modelName,
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content { text("Ты — медицинский ассистент eRecept. Помогаешь врачам анализировать рецепты и отвечать на вопросы пациентов.") }
    )

    fun updateModel(modelName: String) {
        generativeModel = createModel(modelName)
        _uiState.update { it.copy(selectedModel = modelName) }
    }

    fun loadChat(chatId: String) {
        _uiState.update { it.copy(currentChatId = chatId, isLoading = false) }
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.getMessagesForChat(chatId).collect { entities ->
                val messages = entities.map {
                    AiMessage(id = it.id, text = it.text, isUser = it.isUser)
                }
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    fun createNewChat() {
        val newChatId = UUID.randomUUID().toString()
        _uiState.update { it.copy(currentChatId = newChatId, messages = emptyList()) }
        messagesJob?.cancel()
    }

    fun sendMessage(userText: String, image: Bitmap? = null) {
        val chatId = _uiState.value.currentChatId ?: UUID.randomUUID().toString().also {
            _uiState.update { state -> state.copy(currentChatId = it) }
        }

        val userMsgId = UUID.randomUUID().toString()
        val userMsgEntity = MessageEntity(id = userMsgId, chatId = chatId, text = userText, isUser = true)

        viewModelScope.launch {
            if (_uiState.value.messages.isEmpty()) {
                val title = userText.take(30) + if (userText.length > 30) "..." else ""
                repository.createChat(chatId, title)
            }

            repository.saveMessage(userMsgEntity)
            _uiState.update { it.copy(isLoading = true) }

            try {
                val historyEntities = repository.getMessagesForChatSync(chatId)
                val history = historyEntities.filter { it.id != userMsgId }.map { msg ->
                    content(if (msg.isUser) "user" else "model") { text(msg.text) }
                }

                val chat = generativeModel.startChat(history)

                val response = if (image != null) {
                    generativeModel.generateContent(content { image(image); text(userText) })
                } else {
                    chat.sendMessage(userText)
                }

                val aiText = response.text ?: "Не удалось получить ответ"
                val aiMsgEntity = MessageEntity(id = UUID.randomUUID().toString(), chatId = chatId, text = aiText, isUser = false)

                repository.saveMessage(aiMsgEntity)

            } catch (e: Exception) {
                val errorText = if (e.message?.contains("503") == true || e.message?.contains("MissingFieldException") == true) {
                    "ИИ сейчас сильно перегружен запросами. Пожалуйста, подождите минутку и попробуйте снова."
                } else {
                    "Произошла ошибка: ${e.message}"
                }
                repository.saveMessage(MessageEntity(id = UUID.randomUUID().toString(), chatId = chatId, text = errorText, isUser = false))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    fun renameChat(chatId: String, newTitle: String) {
        viewModelScope.launch {
            repository.updateChatTitle(chatId, newTitle)
        }
    }

    fun editMessage(messageId: String, newText: String) {
        val chatId = _uiState.value.currentChatId ?: return

        viewModelScope.launch {
            val messages = repository.getMessagesForChatSync(chatId)
            val targetMessage = messages.find { it.id == messageId } ?: return@launch

            repository.deleteMessagesFromTimestamp(chatId, targetMessage.timestamp)

            sendMessage(newText)
        }
    }

    fun regenerateLastResponse() {
        val chatId = _uiState.value.currentChatId ?: return

        viewModelScope.launch {
            val messagesEntities = repository.getMessagesForChatSync(chatId)
            if (messagesEntities.isEmpty()) return@launch

            val lastUserMsg = messagesEntities.lastOrNull { it.isUser } ?: return@launch
            val lastMsg = messagesEntities.last()

            if (!lastMsg.isUser) {
                repository.deleteMessage(lastMsg.id)
            }

            _uiState.update { it.copy(isLoading = true) }

            try {
                val historyEntities = repository.getMessagesForChatSync(chatId)
                val history = historyEntities.filter { it.id != lastUserMsg.id }.map { msg ->
                    content(if (msg.isUser) "user" else "model") { text(msg.text) }
                }

                val chat = generativeModel.startChat(history)
                val response = chat.sendMessage(lastUserMsg.text)

                val aiText = response.text ?: "Не удалось получить ответ"
                val aiMsgEntity = MessageEntity(id = UUID.randomUUID().toString(), chatId = chatId, text = aiText, isUser = false)

                repository.saveMessage(aiMsgEntity)
            } catch (e: Exception) {
                val errorMsg = MessageEntity(id = UUID.randomUUID().toString(), chatId = chatId, text = "Ошибка перегенерации: ${e.localizedMessage}", isUser = false)
                repository.saveMessage(errorMsg)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}