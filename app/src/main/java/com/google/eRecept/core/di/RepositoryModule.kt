package com.google.eRecept.core.di

import android.content.Context
import com.google.eRecept.feature.auth.repository.AuthRepository
import com.google.eRecept.feature.home.repository.HomeRepository
import com.google.eRecept.feature.recipe.repository.RecipeRepository
import com.google.eRecept.feature.search.repository.SearchRepository
import com.google.eRecept.data.network.api.AuthApi
import com.google.eRecept.data.network.api.HomeApi
import com.google.eRecept.data.network.api.RecipeApi
import com.google.eRecept.data.network.api.SearchApi
import com.google.eRecept.feature.auth.repository.AuthRepositoryImpl
import com.google.eRecept.feature.home.repository.HomeRepositoryImpl
import com.google.eRecept.feature.recipe.repository.RecipeRepositoryImpl
import com.google.eRecept.feature.search.repository.SearchRepositoryImpl
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
        return AuthRepositoryImpl(api)
    }
    @Provides
    @Singleton
    fun provideHomeRepository(
        api: HomeApi,
        @ApplicationContext context: Context,
    ): HomeRepository = HomeRepositoryImpl(api, context)
    @Provides
    @Singleton
    fun provideRecipeRepository(
        api: RecipeApi,
        @ApplicationContext context: Context,
    ): RecipeRepository = RecipeRepositoryImpl(api, context)
    @Provides
    @Singleton
    fun provideSearchRepository(
        api: SearchApi,
        @ApplicationContext context: Context,
    ): SearchRepository = SearchRepositoryImpl(api, context)
}
