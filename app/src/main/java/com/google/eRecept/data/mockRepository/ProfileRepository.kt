package com.google.eRecept.data.mockRepository

import com.google.eRecept.data.Doctor
import kotlinx.coroutines.delay

interface ProfileRepository {
    val currentUserId: String?

    suspend fun getDoctorProfile(doctorId: String): Doctor?
}

class MockProfileRepository : ProfileRepository {
    override val currentUserId: String = "mock_doctor_id"

    override suspend fun getDoctorProfile(doctorId: String): Doctor? {
        delay(300)
        return Doctor(
            id = currentUserId,
            name = "Д-р Хаус",
            specialization = "Врач-терапевт высшей категории",
        )
    }
}
