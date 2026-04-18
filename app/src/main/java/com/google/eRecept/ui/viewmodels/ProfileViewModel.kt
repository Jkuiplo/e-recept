package com.google.eRecept.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.data.Doctor
import com.google.eRecept.data.mockRepository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel
    @Inject
    constructor(
        private val repository: ProfileRepository,
    ) : ViewModel() {
        private val _doctorProfile = MutableStateFlow<Doctor?>(null)
        val doctorProfile: StateFlow<Doctor?> = _doctorProfile.asStateFlow()

        init {
            repository.currentUserId?.let { doctorId ->
                viewModelScope.launch {
                    _doctorProfile.value = repository.getDoctorProfile(doctorId)
                }
            }
        }
    }
