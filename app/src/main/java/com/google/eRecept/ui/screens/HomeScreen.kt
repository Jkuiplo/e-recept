package com.google.eRecept.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.eRecept.ui.theme.MainAc
import com.google.eRecept.ui.theme.PrimaryPurple
import com.google.eRecept.ui.theme.SecBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onProfileClick: () -> Unit = {},
    onCreateRecipeClick: () -> Unit = {},
    onSearchPatientsClick: () -> Unit = {},
    onSearchMedsClick: () -> Unit = {},
    onAddPatientClick: () -> Unit = {},
) {
    // Получаем текущую дату в нужном формате ("Понедельник, 7 апреля")
    val currentDate =
        remember {
            val sdf = SimpleDateFormat("EEEE, d MMMM", Locale("ru"))
            sdf.format(Date()).replaceFirstChar { it.titlecase(Locale("ru")) }
        }

    // Заглушка для расписания
    val schedule =
        listOf(
            Appointment(
                time = "09:00",
                name = "Қазыбек Нұрым",
                age = "68 лет",
                gender = "М",
                status = "Состоялась",
                isCompleted = true,
                allergy = "пенициллин",
            ),
            Appointment(
                time = "11:30",
                name = "Иванова Анна",
                age = "34 года",
                gender = "Ж",
                status = "Запланирована",
                isCompleted = false,
                allergy = null,
            ),
            Appointment(
                time = "14:00",
                name = "Смирнов Петр",
                age = "45 лет",
                gender = "М",
                status = "Запланирована",
                isCompleted = false,
                allergy = null,
            ),
        )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Шапка: Сегодня + Дата и Аватар
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Сегодня",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Аватар врача
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple)
                        .clickable { onProfileClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "А", // Инициал
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Главная кнопка действия
        Button(
            onClick = onAddPatientClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = SecBg, // Приглушенный цвет по ТЗ
                    contentColor = MainAc,
                ),
        ) {
            Text(
                text = "+ Записать пациента",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Расписание",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Список приемов
        if (schedule.isEmpty()) {
            EmptyScheduleState(onAddPatientClick)
        } else {
            schedule.forEach { appointment ->
                AppointmentCard(appointment)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Модель данных для приема
data class Appointment(
    val time: String,
    val name: String,
    val age: String,
    val gender: String,
    val status: String,
    val isCompleted: Boolean,
    val allergy: String?,
)

@Composable
fun AppointmentCard(appointment: Appointment) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { /* TODO: Открыть детальный экран записи */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Время приема слева
            Text(
                text = appointment.time,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.width(60.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Данные пациента по центру
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "${appointment.age} · ${appointment.gender}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Предупреждение об аллергии (если есть)
                if (appointment.allergy != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚠ аллергия: ${appointment.allergy}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            // Бейдж статуса справа
            val badgeColor = if (appointment.isCompleted) Color(0xFF10B981) else Color(0xFFF59E0B)
            val badgeBg = badgeColor.copy(alpha = 0.15f)

            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = appointment.status,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = badgeColor,
                )
            }
        }
    }
}

@Composable
fun EmptyScheduleState(onAddPatientClick: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Записей на сегодня нет",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
