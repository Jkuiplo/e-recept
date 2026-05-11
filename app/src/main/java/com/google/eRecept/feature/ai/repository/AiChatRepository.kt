package com.google.eRecept.feature.ai.repository

import com.google.eRecept.data.local.dao.AiChatDao
import com.google.eRecept.data.local.entity.ChatEntity
import com.google.eRecept.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface AiChatRepository {
    fun getAllChats(): Flow<List<ChatEntity>>
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>
    suspend fun getMessagesForChatSync(chatId: String): List<MessageEntity>
    suspend fun createChat(chatId: String, title: String)
    suspend fun saveMessage(message: MessageEntity)
    suspend fun deleteChat(chatId: String)

    suspend fun updateChatTitle(chatId: String, newTitle: String)
    suspend fun deleteMessage(messageId: String)
    suspend fun deleteMessagesFromTimestamp(chatId: String, timestamp: Long)
}

@Singleton
class AiChatRepositoryImpl @Inject constructor(
    private val dao: AiChatDao
) : AiChatRepository {
    override fun getAllChats() = dao.getAllChats()
    override fun getMessagesForChat(chatId: String) = dao.getMessagesForChat(chatId)
    override suspend fun getMessagesForChatSync(chatId: String) = dao.getMessagesForChatSync(chatId)
    override suspend fun createChat(chatId: String, title: String) {
        dao.insertChat(ChatEntity(id = chatId, title = title))
    }
    override suspend fun saveMessage(message: MessageEntity) {
        dao.insertMessage(message)
    }
    override suspend fun deleteChat(chatId: String) {
        dao.deleteChat(chatId)
    }
    override suspend fun updateChatTitle(chatId: String, newTitle: String) = dao.updateChatTitle(chatId, newTitle)
    override suspend fun deleteMessage(messageId: String) = dao.deleteMessage(messageId)
    override suspend fun deleteMessagesFromTimestamp(chatId: String, timestamp: Long) = dao.deleteMessagesFromTimestamp(chatId, timestamp)
}