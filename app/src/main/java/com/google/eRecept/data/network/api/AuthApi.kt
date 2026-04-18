package com.google.eRecept.data.network.api

import com.google.eRecept.data.network.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("login")
    suspend fun login(
        @Body request: LoginRequest,
    ): Response<LoginResponse>

    @POST("forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest,
    ): Response<BaseMessageResponse>

    @POST("reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest,
    ): Response<BaseMessageResponse>
}
