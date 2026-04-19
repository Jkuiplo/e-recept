package com.google.eRecept.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.google.eRecept.data.Doctor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel
    @Inject
    constructor(
        application: Application,
    ) : AndroidViewModel(application) {
        private val prefs = application.getSharedPreferences("erecept_prefs", Context.MODE_PRIVATE)

        private val _doctorProfile = MutableStateFlow<Doctor?>(null)
        val doctorProfile: StateFlow<Doctor?> = _doctorProfile.asStateFlow()

        // 0 = Светлая, 1 = Темная, 2 = Система (по умолчанию)
        private val _themeMode = MutableStateFlow(prefs.getInt("theme_mode", 2))
        val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

        init {
            loadProfile()
        }

        private fun loadProfile() {
            val id = prefs.getString("doctor_id", "") ?: ""
            val name = prefs.getString("doctor_name", "Врач") ?: "Врач"
            val specialization = prefs.getString("doctor_specialization", "Специалист") ?: "Специалист"

            _doctorProfile.value = Doctor(id = id, name = name, specialization = specialization)
        }

        fun updateTheme(index: Int) {
            prefs.edit().putInt("theme_mode", index).apply()
            _themeMode.value = index
        }
    }
