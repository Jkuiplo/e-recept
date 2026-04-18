package com.google.eRecept.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
    @Inject
    constructor(
        application: Application,
        private val repository: AuthRepository,
    ) : AndroidViewModel(application) {
        private val prefs = application.getSharedPreferences("erecept_prefs", Context.MODE_PRIVATE)

        private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
        val authState: StateFlow<AuthState> = _authState

        private val _savedEmail = MutableStateFlow(prefs.getString("saved_email", "") ?: "")
        val savedEmail: StateFlow<String> = _savedEmail

        // Для показа Toast/Snackbar (одноразовые события)
        private val _uiMessage = MutableSharedFlow<String>()
        val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

        init {
            checkCurrentUser()
        }

        private fun checkCurrentUser() {
            if (repository.isUserLoggedIn()) {
                _authState.value = AuthState.Authenticated
            }
        }

        fun login(
            email: String,
            password: String,
            rememberMe: Boolean,
        ) {
            if (email.isBlank() ||
                !android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches()
            ) {
                _authState.value = AuthState.Error("Введите корректный email")
                return
            }

            _authState.value = AuthState.Loading

            viewModelScope.launch {
                val result = repository.login(email, password)
                result.fold(
                    onSuccess = {
                        if (rememberMe) {
                            prefs.edit { putString("saved_email", email) }
                        } else {
                            prefs.edit { remove("saved_email") }
                        }
                        _authState.value = AuthState.Authenticated
                    },
                    onFailure = { exception ->
                        _authState.value = AuthState.Error(exception.message ?: "Неизвестная ошибка")
                    },
                )
            }
        }

        fun forgotPassword(
            email: String,
            onSuccess: () -> Unit,
        ) {
            viewModelScope.launch {
                val result = repository.forgotPassword(email)
                result.fold(
                    onSuccess = { message ->
                        _uiMessage.emit(message)
                        onSuccess()
                    },
                    onFailure = { error -> _uiMessage.emit(error.message ?: "Ошибка") },
                )
            }
        }

        fun resetPassword(
            token: String,
            newPassword: String,
            onSuccess: () -> Unit,
        ) {
            viewModelScope.launch {
                val result = repository.resetPassword(token, newPassword)
                result.fold(
                    onSuccess = { message ->
                        _uiMessage.emit(message)
                        onSuccess()
                    },
                    onFailure = { error -> _uiMessage.emit(error.message ?: "Ошибка") },
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
