package com.google.eRecept.di

import com.google.eRecept.data.mockRepository.*
import com.google.eRecept.data.network.api.AuthApi
import com.google.eRecept.data.repository.NetworkAuthRepository
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
    fun provideAuthRepository(api: AuthApi): AuthRepository {
        return NetworkAuthRepository(api) // Потом заменишь на NetworkAuthRepository(api)
    }
    // fun provideAuthRepository(): AuthRepository {
    //      return MockAuthRepository()
    // }

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
