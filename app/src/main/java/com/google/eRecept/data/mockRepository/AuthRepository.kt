package com.google.eRecept.data.mockRepository

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

class MockAuthRepository : AuthRepository {
    private var isLoggedIn = false

    override suspend fun login(
        email: String,
        password: String,
    ): Result<LoginResponse> {
        delay(1500)
        return if (email == "dr.ivanov@clinic.kz" && password == "securepassword123") {
            isLoggedIn = true
            Result.success(
                LoginResponse(
                    accessToken = "mock_jwt_token_123",
                    tokenType = "bearer",
                    doctorId = "mock_doctor_id",
                    fullName = "Иванов Иван Иванович",
                    specialization = "Терапевт",
                ),
            )
        } else {
            Result.failure(Exception("Неверная почта или пароль."))
        }
    }

    override suspend fun forgotPassword(email: String): Result<String> {
        delay(1000)
        return if (email.isNotBlank()) {
            Result.success("Если email зарегистрирован, письмо со ссылкой будет отправлено")
        } else {
            Result.failure(Exception("Введите email"))
        }
    }

    override suspend fun resetPassword(
        token: String,
        newPassword: String,
    ): Result<String> {
        delay(1000)
        return if (token.isNotBlank() && newPassword.length >= 6) {
            Result.success("Пароль успешно изменён. Войдите с новым паролем.")
        } else {
            Result.failure(Exception("Недействительный токен или короткий пароль"))
        }
    }

    override fun logout() {
        isLoggedIn = false
    }

    override fun isUserLoggedIn(): Boolean = isLoggedIn
}
