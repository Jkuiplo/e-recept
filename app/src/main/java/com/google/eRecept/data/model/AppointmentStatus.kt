package com.google.eRecept.data.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.eRecept.R

enum class AppointmentStatus(val backendValue: String) {
    PLANNED("Запланирована"),
    COMPLETED("Состоялась"),
    NO_SHOW("Не явился"),
    CANCELLED("Отменена"),
    UNKNOWN("");

    companion object {
        fun fromBackendString(value: String): AppointmentStatus {
            val normalized = value.trim()

            val match = entries.find { it.backendValue.equals(normalized, ignoreCase = true) }

            return match ?: if (normalized.equals("Запланирована", ignoreCase = true)) PLANNED else UNKNOWN
        }
    }
}

@Composable
fun AppointmentStatus.toLocalizedString(): String {
    return when (this) {
        AppointmentStatus.PLANNED -> stringResource(R.string.status_planned)
        AppointmentStatus.COMPLETED -> stringResource(R.string.status_completed)
        AppointmentStatus.NO_SHOW -> stringResource(R.string.status_didnt_came)
        AppointmentStatus.CANCELLED -> stringResource(R.string.status_cancel)
        AppointmentStatus.UNKNOWN -> "Unknown"
    }
}