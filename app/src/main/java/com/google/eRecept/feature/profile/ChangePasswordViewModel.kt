package com.google.eRecept.feature.profile

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.data.network.api.AuthApi
import com.google.eRecept.data.network.dto.ChangePasswordRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()

    object Loading : ChangePasswordState()

    object Success : ChangePasswordState()

    data class Error(
        val message: String,
    ) : ChangePasswordState()
}

@HiltViewModel
class ChangePasswordViewModel
    @Inject
    constructor(
        private val authApi: AuthApi, // Или ваш репозиторий, если вы используете его между ViewModel и API
        application: Application,
    ) : AndroidViewModel(application) {
        private val prefs = application.getSharedPreferences("erecept_prefs", Context.MODE_PRIVATE)

        private val _uiState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
        val uiState: StateFlow<ChangePasswordState> = _uiState.asStateFlow()

        fun changePassword(
            oldPassword: String,
            newPassword: String,
        ) {
            val doctorId = prefs.getString("doctor_id", "") ?: ""

            if (doctorId.isEmpty()) {
                _uiState.value = ChangePasswordState.Error("ID врача не найден")
                return
            }

            _uiState.value = ChangePasswordState.Loading

            viewModelScope.launch {
                try {
                    val request =
                        ChangePasswordRequest(
                            doctorId = doctorId,
                            oldPassword = oldPassword,
                            newPassword = newPassword,
                        )
                    val response = authApi.changePassword(request)

                    if (response.isSuccessful) {
                        _uiState.value = ChangePasswordState.Success
                    } else {
                        _uiState.value = ChangePasswordState.Error("Ошибка: ${response.code()}")
                    }
                } catch (e: Exception) {
                    _uiState.value = ChangePasswordState.Error(e.localizedMessage ?: "Неизвестная ошибка")
                }
            }
        }

        fun resetState() {
            _uiState.value = ChangePasswordState.Idle
        }
    }
