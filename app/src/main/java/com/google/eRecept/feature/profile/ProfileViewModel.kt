package com.google.eRecept.feature.profile

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.core.utils.getStringFlow
import com.google.eRecept.data.model.Doctor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import androidx.core.content.edit

@HiltViewModel
class ProfileViewModel
    @Inject
    constructor(
        application: Application,
    ) : AndroidViewModel(application) {
        private val prefs = application.getSharedPreferences("erecept_prefs", Context.MODE_PRIVATE)

        val doctorName: StateFlow<String> =
            prefs
                .getStringFlow("doctor_name", "Неизвестно")
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Companion.WhileSubscribed(5000),
                    initialValue = prefs.getString("doctor_name", "Неизвестно") ?: "",
                )

        private val _doctorProfile = MutableStateFlow<Doctor?>(null)
        val doctorProfile: StateFlow<Doctor?> = _doctorProfile.asStateFlow()

        private val _themeMode = MutableStateFlow(prefs.getInt("theme_mode", 2))
        val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

        private val _language = MutableStateFlow(prefs.getString("language", "ru") ?: "ru")
        val language: StateFlow<String> = _language.asStateFlow()

        init {
            loadProfile()
            if (!prefs.contains("language")) {
                prefs.edit { putString("language", "ru") }
            }
        }

        private fun loadProfile() {
            val id = prefs.getString("doctor_id", "") ?: ""
            val specialization = prefs.getString("doctor_specialization", "Специалист") ?: "Специалист"

            _doctorProfile.value =
                Doctor(id = id, name = doctorName.value, specialization = specialization)
        }

        fun updateTheme(index: Int) {
            prefs.edit { putInt("theme_mode", index) }
            _themeMode.value = index
        }

        fun updateLanguage(tag: String) {
            prefs.edit { putString("language", tag) }
            _language.value = tag
        }
    }
