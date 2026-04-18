package com.google.eRecept.di

import com.google.eRecept.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return MockAuthRepository() // Потом заменишь на NetworkAuthRepository(api)
    }

    @Provides
    @Singleton
    fun provideHomeRepository(): HomeRepository = MockHomeRepository()

    @Provides
    @Singleton
    fun provideProfileRepository(): ProfileRepository = MockProfileRepository()

    @Provides
    @Singleton
    fun provideRecipeRepository(): RecipeRepository = MockRecipeRepository()

    @Provides
    @Singleton
    fun provideSearchRepository(): SearchRepository = MockSearchRepository()
}
