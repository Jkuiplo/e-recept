package com.google.eRecept.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.eRecept.data.Doctor
import com.google.eRecept.data.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: FirebaseRepository = FirebaseRepository()) : ViewModel() {
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
