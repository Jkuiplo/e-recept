package com.google.eRecept.feature.auth.repository

import com.google.eRecept.data.network.dto.LoginResponse
import kotlinx.coroutines.delay

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
    ): Result<LoginResponse>

    suspend fun forgotPassword(email: String): Result<String>

    suspend fun resetPassword(
        token: String,
        newPassword: String,
    ): Result<String>

    fun logout()

    fun isUserLoggedIn(): Boolean
}