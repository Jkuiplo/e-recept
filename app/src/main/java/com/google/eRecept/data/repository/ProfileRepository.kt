package com.google.eRecept.data.repository

import com.google.eRecept.data.Doctor
import kotlinx.coroutines.delay

// Контракт
interface ProfileRepository {
    val currentUserId: String?

    suspend fun getDoctorProfile(doctorId: String): Doctor?
}

// Заглушка
class MockProfileRepository : ProfileRepository {
    override val currentUserId: String = "mock_doctor_id"

    override suspend fun getDoctorProfile(doctorId: String): Doctor? {
        delay(300) // Легкая имитация загрузки из сети
        return Doctor(
            id = currentUserId,
            name = "Д-р Хаус",
            specialization = "Врач-терапевт высшей категории",
        )
    }
}
