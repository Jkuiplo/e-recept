package com.google.eRecept.data.repository

import kotlinx.coroutines.delay

// 1. Наше "Меню" (Контракт)
interface AuthRepository {
    suspend fun login(
        iin: String,
        password: String,
    ): Result<Unit>

    fun logout()

    fun isUserLoggedIn(): Boolean
}

// 2. Наш "Повар-стажер" (Заглушка без бэкенда)
class MockAuthRepository : AuthRepository {
    private var isLoggedIn = false

    override suspend fun login(
        iin: String,
        password: String,
    ): Result<Unit> {
        // Имитируем задержку сети (1.5 секунды), чтобы крутился индикатор загрузки
        delay(1500)

        // Тестовые данные для входа. Потом мы это удалим.
        return if (iin == "123456789012" && password == "123456") {
            isLoggedIn = true
            Result.success(Unit) // Успешно
        } else {
            Result.failure(Exception("Неверный ИИН или пароль. Для теста используй ИИН 123456789012 и пароль 123456"))
        }
    }

    override fun logout() {
        isLoggedIn = false
    }

    override fun isUserLoggedIn(): Boolean = isLoggedIn
}
