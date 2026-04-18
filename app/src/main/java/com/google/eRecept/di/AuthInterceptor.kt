package com.google.eRecept.di

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val prefs = context.getSharedPreferences("erecept_prefs", Context.MODE_PRIVATE)
            val token = prefs.getString("access_token", null)

            val requestBuilder = chain.request().newBuilder()
            if (!token.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            return chain.proceed(requestBuilder.build())
        }
    }
