package com.google.eRecept.data.repository

import com.google.eRecept.data.mockRepository.AuthRepository
import com.google.eRecept.data.network.api.AuthApi
import com.google.eRecept.data.network.dto.ForgotPasswordRequest
import com.google.eRecept.data.network.dto.LoginRequest
import com.google.eRecept.data.network.dto.LoginResponse
import com.google.eRecept.data.network.dto.ResetPasswordRequest
import javax.inject.Inject

class NetworkAuthRepository
    @Inject
    constructor(
        private val api: AuthApi,
    ) : AuthRepository {
        // Для сети мы пока временно считаем юзера залогиненным, если у нас есть токен в префах.
        // Это будет реализовано позже во ViewModel, поэтому пока возвращаем false,
        // чтобы заставить юзера залогиниться.
        override fun isUserLoggedIn(): Boolean = false

        override suspend fun login(
            email: String,
            password: String,
        ): Result<LoginResponse> =
            try {
                val response = api.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    // Если статус 401, 400 и тд
                    Result.failure(Exception("Ошибка авторизации: ${response.code()}"))
                }
            } catch (e: Exception) {
                // Если нет интернета или сервак упал
                Result.failure(Exception("Ошибка сети: ${e.message}"))
            }

        override suspend fun forgotPassword(email: String): Result<String> =
            try {
                val response = api.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.detail)
                } else {
                    Result.failure(Exception("Ошибка: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Ошибка сети: ${e.message}"))
            }

        override suspend fun resetPassword(
            token: String,
            newPassword: String,
        ): Result<String> =
            try {
                val response = api.resetPassword(ResetPasswordRequest(token, newPassword))
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.detail)
                } else {
                    Result.failure(Exception("Ошибка: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Ошибка сети: ${e.message}"))
            }

        override fun logout() {
            // Очистка токена будет происходить во ViewModel
        }
    }
