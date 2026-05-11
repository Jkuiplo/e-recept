package com.google.eRecept.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.google.eRecept.data.local.entity.ChatEntity
import com.google.eRecept.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiChatDao {
    @Query("SELECT * FROM ai_chats ORDER BY createdAt DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM ai_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM ai_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    suspend fun getMessagesForChatSync(chatId: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM ai_chats WHERE id = :chatId")
    suspend fun deleteChat(chatId: String)

    @Query("UPDATE ai_chats SET title = :newTitle WHERE id = :chatId")
    suspend fun updateChatTitle(chatId: String, newTitle: String)

    @Query("DELETE FROM ai_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM ai_messages WHERE chatId = :chatId AND timestamp >= :timestamp")
    suspend fun deleteMessagesFromTimestamp(chatId: String, timestamp: Long)
}