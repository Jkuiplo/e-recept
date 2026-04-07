package com.google.eRecept.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProfileClick: () -> Unit = {},
    onCreateRecipeClick: () -> Unit = {},
    onSearchPatientsClick: () -> Unit = {},
    onSearchMedsClick: () -> Unit = {},
    // onAddPatientClick можно оставить для внешней логики, но теперь открытие шторки обрабатывается внутри
    onAddPatientClick: () -> Unit = {},
) {
    // Состояния для отображения BottomSheet
    var showAddPatientSheet by remember { mutableStateOf(false) }
    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentDate =
        remember {
            val sdf = SimpleDateFormat("EEEE, d MMMM", Locale("ru"))
            sdf.format(Date()).replaceFirstChar { it.titlecase(Locale("ru")) }
        }

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
                history = "Хронический бронхит, артериальная гипертензия.",
            ),
            Appointment(
                time = "11:30",
                name = "Иванова Анна",
                age = "34 года",
                gender = "Ж",
                status = "Запланирована",
                isCompleted = false,
                allergy = null,
                history = "Жалоб нет, плановый осмотр.",
            ),
            Appointment(
                time = "14:00",
                name = "Смирнов Петр",
                age = "45 лет",
                gender = "М",
                status = "Запланирована",
                isCompleted = false,
                allergy = null,
                history = "Остеохондроз поясничного отдела.",
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

        // Шапка
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

            // Аватар врача (адаптивный цвет)
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onProfileClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "А",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Главная кнопка действия -> открывает шторку
        Button(
            onClick = { showAddPatientSheet = true },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
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

        if (schedule.isEmpty()) {
            EmptyScheduleState { showAddPatientSheet = true }
        } else {
            schedule.forEach { appointment ->
                AppointmentCard(
                    appointment = appointment,
                    onClick = { selectedAppointment = appointment },
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ModalBottomSheet для добавления пациента (поиск по ИИН)
    if (showAddPatientSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddPatientSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            AddPatientBottomSheetContent(
                onClose = { showAddPatientSheet = false },
            )
        }
    }

    // ModalBottomSheet для деталей приема
    if (selectedAppointment != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedAppointment = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            AppointmentDetailsBottomSheetContent(
                appointment = selectedAppointment!!,
                onCreateRecipeClick = {
                    selectedAppointment = null // закрываем шторку перед переходом
                    onCreateRecipeClick()
                },
            )
        }
    }
}

data class Appointment(
    val time: String,
    val name: String,
    val age: String,
    val gender: String,
    val status: String,
    val isCompleted: Boolean,
    val allergy: String?,
    val history: String = "",
)

@Composable
fun AppointmentCard(
    appointment: Appointment,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Время приема с увеличенной минимальной шириной, чтобы влезало в 1 строку
            Text(
                text = appointment.time,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.defaultMinSize(minWidth = 72.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${appointment.age} · ${appointment.gender}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (appointment.allergy != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚠ Аллергия: ${appointment.allergy}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            // Адаптивные цвета для бейджа
            val badgeColor = if (appointment.isCompleted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
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
fun AddPatientBottomSheetContent(onClose: () -> Unit) {
    var iin by remember { mutableStateOf("") }
    var patientName by remember { mutableStateOf("") }
    var patientAge by remember { mutableStateOf("") }
    var patientGender by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Простая заглушка (Mock) для проверки ИИН
    fun searchPatient(query: String) {
        if (query.length == 12) {
            if (query == "123456789012") {
                patientName = "Иванов Иван Иванович"
                patientAge = "45 лет"
                patientGender = "Мужской"
                errorMessage = ""
            } else {
                patientName = ""
                patientAge = ""
                patientGender = ""
                errorMessage = "Пациент с таким ИИН не найден"
            }
        } else {
            patientName = ""
            patientAge = ""
            patientGender = ""
            errorMessage = ""
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 40.dp),
    ) {
        Text(
            text = "Запись пациента",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = iin,
            onValueChange = {
                // Разрешаем вводить только цифры, максимум 12 символов
                if (it.length <= 12 && it.all { char -> char.isDigit() }) {
                    iin = it
                    searchPatient(it)
                }
            },
            label = { Text("ИИН пациента") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = errorMessage.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Автоматически заполняемые поля (только для чтения)
        OutlinedTextField(
            value = patientName,
            onValueChange = {},
            label = { Text("ФИО") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = patientAge,
                onValueChange = {},
                label = { Text("Возраст") },
                readOnly = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = patientGender,
                onValueChange = {},
                label = { Text("Пол") },
                readOnly = true,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onClose,
            enabled = patientName.isNotEmpty(), // Кнопка активна только если пациент найден
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("Подтвердить запись")
        }
    }
}

@Composable
fun AppointmentDetailsBottomSheetContent(
    appointment: Appointment,
    onCreateRecipeClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 40.dp),
    ) {
        Text(
            text = "Детали приема",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = appointment.name,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "${appointment.age} · Пол: ${appointment.gender}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "История болезни:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = appointment.history.ifEmpty { "Нет данных" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Аллергии:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = appointment.allergy ?: "Не выявлено",
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (appointment.allergy != null) FontWeight.Bold else FontWeight.Normal,
                ),
            color = if (appointment.allergy != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCreateRecipeClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
        ) {
            Text("Создать рецепт", style = MaterialTheme.typography.titleMedium)
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
