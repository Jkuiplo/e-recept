package com.google.eRecept.core.di

import android.content.Context
import androidx.room.Room
import com.google.eRecept.data.local.AppDatabase
import com.google.eRecept.data.local.dao.AiChatDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "erecept_database"
        ).build()
    }

    @Provides
    fun provideAiChatDao(database: AppDatabase): AiChatDao {
        return database.aiChatDao()
    }
}