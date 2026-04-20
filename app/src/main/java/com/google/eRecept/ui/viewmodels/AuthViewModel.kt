package com.google.eRecept.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.data.mockRepository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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

        private val _uiMessage = MutableSharedFlow<String>()
        val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

        init {
            checkCurrentUser()
        }

        private fun checkCurrentUser() {
            val rememberMe = prefs.getBoolean("remember_me", false)
            val token = prefs.getString("access_token", null)
            if (!rememberMe) {
                prefs.edit {
                    remove("access_token")
                    remove("doctor_id")
                    remove("doctor_name")
                    remove("doctor_specialization")
                }
                _authState.value = AuthState.Unauthenticated
                return
            }

            if (!token.isNullOrBlank() || repository.isUserLoggedIn()) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }

        fun login(
            email: String,
            password: String,
            rememberMe: Boolean,
        ) {
            // ... проверки валидации оставляем без изменений ...

            viewModelScope.launch {
                val result = repository.login(email, password)
                result.fold(
                    onSuccess = { loginResponse ->
                        prefs.edit {
                            putString("access_token", loginResponse.accessToken)
                            putString("doctor_id", loginResponse.doctorId)
                            putString("doctor_name", loginResponse.fullName)
                            putString("doctor_specialization", loginResponse.specialization)

                            // Сохраняем выбор пользователя для следующего запуска
                            putBoolean("remember_me", rememberMe)

                            if (rememberMe) {
                                putString("saved_email", email)
                            } else {
                                remove("saved_email")
                            }
                        }
                        _authState.value = AuthState.Authenticated
                    },
                    onFailure = { exception ->
                        if (exception.message == "NO_INTERNET") {
                            _authState.value = AuthState.NoInternet
                        } else {
                            _authState.value = AuthState.Error(exception.message ?: "Неизвестная ошибка")
                        }
                    },
                )
            }
        }

        fun resetState() {
            _authState.value = AuthState.Idle
        }

        private val _resendTimer = MutableStateFlow(0)
        val resendTimer: StateFlow<Int> = _resendTimer

        fun startResendTimer() {
            viewModelScope.launch {
                _resendTimer.value = 60
                while (_resendTimer.value > 0) {
                    delay(1000)
                    _resendTimer.value -= 1
                }
            }
        }

        fun forgotPassword(
            email: String,
            onEmailSent: () -> Unit,
        ) {
            _authState.value = AuthState.Loading
            viewModelScope.launch {
                val result = repository.forgotPassword(email)
                result.fold(
                    onSuccess = { message ->
                        _uiMessage.emit(message)
                        _authState.value = AuthState.Idle
                        onEmailSent()
                        startResendTimer()
                    },
                    onFailure = { exception ->
                        if (exception.message == "NO_INTERNET") {
                            _authState.value = AuthState.NoInternet
                        } else {
                            _authState.value = AuthState.Error(exception.message ?: "Неизвестная ошибка")
                        }
                    },
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
            prefs.edit {
                remove("access_token")
                remove("doctor_id")
                remove("doctor_name")
                remove("doctor_specialization")
                remove("remember_me")
                remove("saved_email")
            }
            _authState.value = AuthState.Unauthenticated
        }

        sealed class AuthState {
            object Idle : AuthState()

            object Loading : AuthState()

            object Authenticated : AuthState()

            object Unauthenticated : AuthState()

            object NoInternet : AuthState()

            data class Error(
                val message: String,
            ) : AuthState()
        }
    }
