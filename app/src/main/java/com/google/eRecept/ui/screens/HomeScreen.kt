package com.google.eRecept.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.eRecept.ui.components.DateTransformation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProfileClick: () -> Unit = {},
    onCreateRecipeClick: () -> Unit = {},
    onSearchPatientsClick: () -> Unit = {},
    onSearchMedsClick: () -> Unit = {},
    onAddPatientClick: () -> Unit = {},
) {
    var showAddPatientSheet by remember { mutableStateOf(false) }
    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedDayIndex by remember { mutableIntStateOf(0) }
    val days = listOf("Сегодня", "Завтра", "Послезавтра")

    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, selectedDayIndex)
    val selectedDate = calendar.time
    val dateFormatter = SimpleDateFormat("d MMMM, EEEE", Locale("ru"))
    val formattedDate = dateFormatter.format(selectedDate)

    val schedule =
        remember(selectedDayIndex) {
            if (selectedDayIndex == 0) {
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
            } else {
                emptyList()
            }
        }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddPatientSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Записать") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Расписание",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth(),
            ) {
                days.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = days.size),
                        onClick = { selectedDayIndex = index },
                        selected = selectedDayIndex == index,
                    ) {
                        Text(label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Ваш список",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = schedule,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScheduleContent",
            ) { currentSchedule ->
                if (currentSchedule.isEmpty()) {
                    EmptyScheduleState()
                } else {
                    Column(
                        modifier =
                            Modifier
                                .verticalScroll(rememberScrollState()),
                    ) {
                        currentSchedule.forEach { appointment ->
                            AppointmentCard(
                                appointment = appointment,
                                onClick = { selectedAppointment = appointment },
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                    }
                }
            }
        }
    }

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

    if (selectedAppointment != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedAppointment = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            AppointmentDetailsBottomSheetContent(
                appointment = selectedAppointment!!,
                onCreateRecipeClick = {
                    selectedAppointment = null
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
    ElevatedCard(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = appointment.time,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.defaultMinSize(minWidth = 64.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    text = "${appointment.age} · ${appointment.gender}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (appointment.allergy != null) {
                    Text(
                        text = "⚠ Аллергия: ${appointment.allergy}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                    )
                }
            }

            SuggestionChip(
                onClick = { },
                label = {
                    Text(
                        text = appointment.status,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    )
                },
                colors =
                    SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (appointment.isCompleted) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = if (appointment.isCompleted) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                border = null,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddPatientBottomSheetContent(onClose: () -> Unit) {
    var iin by remember { mutableStateOf("") }
    var patientName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    var appointmentDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Первичный") }

    val times = listOf("09:00", "09:20", "09:40", "10:00", "10:20", "10:40", "11:00", "11:20")
    val types = listOf("Первичный", "Повторный", "Консультация", "Осмотр")

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        appointmentDate = sdf.format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    fun searchPatient(query: String) {
        if (query.length == 12) {
            if (query == "123456789012") {
                patientName = "Иванов Иван Иванович"
                errorMessage = ""
            } else {
                patientName = ""
                errorMessage = "Пациент с таким ИИН не найден"
            }
        } else {
            patientName = ""
            errorMessage = ""
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
                .verticalScroll(rememberScrollState()),
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
                if (it.length <= 12 && it.all { char -> char.isDigit() }) {
                    iin = it
                    searchPatient(it)
                }
            },
            label = { Text("ИИН пациента") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            isError = errorMessage.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }

        if (patientName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = patientName,
                onValueChange = {},
                label = { Text("ФИО") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = appointmentDate,
                onValueChange = { if (it.length <= 8) appointmentDate = it },
                label = { Text("Дата приема") },
                placeholder = { Text("ДД.ММ.ГГГГ") },
                visualTransformation = DateTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Время приема", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                times.forEach { time ->
                    FilterChip(
                        selected = selectedTime == time,
                        onClick = { selectedTime = time },
                        label = { Text(time) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Тип приема", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                types.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onClose,
            enabled = patientName.isNotEmpty() && appointmentDate.length >= 8,
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
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
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        ListItem(
            headlineContent = { Text("История болезни") },
            supportingContent = { Text(appointment.history.ifEmpty { "Нет данных" }) },
            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        )

        ListItem(
            headlineContent = { Text("Аллергии") },
            supportingContent = {
                Text(
                    appointment.allergy ?: "Не выявлено",
                    color =
                        if (appointment.allergy !=
                            null
                        ) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            },
            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCreateRecipeClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("Создать рецепт", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun EmptyScheduleState() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Записей пока нет",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "На этот день у вас не запланировано\nни одного приема",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}
