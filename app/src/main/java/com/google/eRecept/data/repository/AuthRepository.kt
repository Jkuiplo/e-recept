package com.google.eRecept.data.repository

import kotlinx.coroutines.delay

// 1. Контракт
interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
    ): Result<Unit>

    // Новые методы из спеки бэкенда
    suspend fun forgotPassword(email: String): Result<String>

    suspend fun resetPassword(
        token: String,
        newPassword: String,
    ): Result<String>

    fun logout()

    fun isUserLoggedIn(): Boolean
}

// 2. Мок
class MockAuthRepository : AuthRepository {
    private var isLoggedIn = false

    override suspend fun login(
        email: String,
        password: String,
    ): Result<Unit> {
        delay(1500)
        // Тестовые данные из документации бэка (п. 2.1)
        return if (email == "dr.ivanov@clinic.kz" && password == "securepassword123") {
            isLoggedIn = true
            Result.success(Unit)
        } else {
            Result.failure(Exception("Неверная почта или пароль. Для теста используй dr.ivanov@clinic.kz / securepassword123"))
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
