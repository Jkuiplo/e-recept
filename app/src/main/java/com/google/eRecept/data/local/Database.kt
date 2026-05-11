package com.google.eRecept.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.google.eRecept.data.local.dao.AiChatDao
import com.google.eRecept.data.local.entity.ChatEntity
import com.google.eRecept.data.local.entity.MessageEntity

@Database(entities = [ChatEntity::class, MessageEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun aiChatDao(): AiChatDao
}