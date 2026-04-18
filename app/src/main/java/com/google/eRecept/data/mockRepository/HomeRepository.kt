package com.google.eRecept.data.mockRepository

import com.google.eRecept.data.Appointment
import com.google.eRecept.data.Patient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

interface HomeRepository {
    val currentUserId: String?

    fun getAppointments(doctorId: String): Flow<List<Appointment>>

    suspend fun getPatientByIin(iin: String): Patient?

    suspend fun createAppointment(appointment: Appointment)

    suspend fun updateAppointmentStatus(
        appointmentId: String,
        newStatus: String,
        isCompleted: Boolean,
    )
}

class MockHomeRepository : HomeRepository {
    override val currentUserId: String = "mock_doctor_id"
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())

    init {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val today = sdf.format(Calendar.getInstance().time)

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = sdf.format(cal.time)

        _appointments.value =
            listOf(
                Appointment(
                    id = "APP-1",
                    doctor_id = currentUserId,
                    patient_iin = "123456789012",
                    patient_name = "Иванов Иван Иванович",
                    date = today,
                    time = "14:20", // Поставил позже, чтобы проверить сортировку
                    history = "Примечание: Жалобы на боли в спине",
                    status = "Запланирован",
                    is_completed = false,
                    age = "30 лет",
                    gender = "Мужской",
                ),
                Appointment(
                    id = "APP-2",
                    doctor_id = currentUserId,
                    patient_iin = "098765432109",
                    patient_name = "Смирнова Анна",
                    date = today,
                    time = "09:00",
                    history = "Примечание: Первичный осмотр",
                    status = "Завершен",
                    is_completed = true,
                    age = "25 лет",
                    gender = "Женский",
                ),
            )
    }

    override fun getAppointments(doctorId: String): Flow<List<Appointment>> = _appointments.asStateFlow()

    override suspend fun getPatientByIin(iin: String): Patient? {
        delay(400)
        return if (iin == "123456789012") {
            Patient(
                iin = "123456789012",
                full_name = "Иванов Иван Иванович",
                birth_date = "1996-05-15",
                gender = "Мужской",
                allergies = "Жалобы на боли в спине",
            )
        } else {
            null
        }
    }

    override suspend fun createAppointment(appointment: Appointment) {
        delay(300)
        val currentList = _appointments.value.toMutableList()
        currentList.add(appointment.copy(id = "APP-${System.currentTimeMillis()}", status = "Запланирован", is_completed = false))
        _appointments.value = currentList
    }

    override suspend fun updateAppointmentStatus(
        appointmentId: String,
        newStatus: String,
        isCompleted: Boolean,
    ) {
        val currentList = _appointments.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == appointmentId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(status = newStatus, is_completed = isCompleted)
            _appointments.value = currentList
        }
    }
}
