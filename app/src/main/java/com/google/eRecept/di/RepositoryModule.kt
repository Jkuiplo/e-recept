package com.google.eRecept.di

import android.content.Context
import com.google.eRecept.data.mockRepository.AuthRepository
import com.google.eRecept.data.mockRepository.HomeRepository
import com.google.eRecept.data.mockRepository.MockProfileRepository
import com.google.eRecept.data.mockRepository.ProfileRepository
import com.google.eRecept.data.mockRepository.RecipeRepository
import com.google.eRecept.data.mockRepository.SearchRepository
import com.google.eRecept.data.network.api.AuthApi
import com.google.eRecept.data.network.api.HomeApi
import com.google.eRecept.data.network.api.RecipeApi
import com.google.eRecept.data.network.api.SearchApi
import com.google.eRecept.data.repository.NetworkAuthRepository
import com.google.eRecept.data.repository.NetworkHomeRepository
import com.google.eRecept.data.repository.NetworkRecipeRepository
import com.google.eRecept.data.repository.NetworkSearchRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideHomeRepository(
        api: HomeApi,
        @ApplicationContext context: Context,
    ): HomeRepository = NetworkHomeRepository(api, context)

//    fun provideHomeRepository(): HomeRepository = MockHomeRepository()

    @Provides
    @Singleton
    fun provideRecipeRepository(
        api: RecipeApi,
        @ApplicationContext context: Context,
    ): RecipeRepository = NetworkRecipeRepository(api, context)
//    fun provideRecipeRepository(): RecipeRepository = MockRecipeRepository()

    @Provides
    @Singleton
    fun provideSearchRepository(
        api: SearchApi,
        @ApplicationContext context: Context,
    ): SearchRepository = NetworkSearchRepository(api, context)
//    fun provideSearchRepository(): SearchRepository = MockSearchRepository()
}
