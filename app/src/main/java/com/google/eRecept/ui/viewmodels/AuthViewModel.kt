package com.google.eRecept.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.data.repository.AuthRepository
import com.google.eRecept.data.repository.MockAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    application: Application,
) : AndroidViewModel(application) {
    // ВРЕМЕННО: Создаем мок напрямую. Когда добавим DI, это будет приходить в конструктор.
    private val repository: AuthRepository = MockAuthRepository()

    private val prefs = application.getSharedPreferences("erecept_prefs", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _savedIin = MutableStateFlow(prefs.getString("saved_iin", "") ?: "")
    val savedIin: StateFlow<String> = _savedIin

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        if (repository.isUserLoggedIn()) {
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(
        iin: String,
        password: String,
        rememberMe: Boolean,
    ) {
        if (iin.length != 12) {
            _authState.value = AuthState.Error("ИИН должен состоять из 12 цифр")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            // Дергаем репозиторий вместо Firebase
            val result = repository.login(iin, password)

            result.fold(
                onSuccess = {
                    if (rememberMe) {
                        prefs.edit().putString("saved_iin", iin).apply()
                    } else {
                        prefs.edit().remove("saved_iin").apply()
                    }
                    _authState.value = AuthState.Authenticated
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Неизвестная ошибка")
                },
            )
        }
    }

    fun logout() {
        repository.logout()
        _authState.value = AuthState.Unauthenticated
    }

    sealed class AuthState {
        object Idle : AuthState()

        object Loading : AuthState()

        object Authenticated : AuthState()

        object Unauthenticated : AuthState()

        data class Error(
            val message: String,
        ) : AuthState()
    }
}
