package com.google.eRecept.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
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

    val days = listOf("Сегодня", "Завтра", "Послезавтра")
    // Создаем PagerState для дней
    val daysPagerState = rememberPagerState(pageCount = { days.size })
    val coroutineScope = rememberCoroutineScope()

    // Динамическая дата в зависимости от текущей страницы пейджера
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, daysPagerState.currentPage)
    val selectedDate = calendar.time
    val dateFormatter = SimpleDateFormat("d MMMM, EEEE", Locale("ru"))
    val formattedDate = dateFormatter.format(selectedDate)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
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

        Spacer(modifier = Modifier.height(16.dp))

        // Привязываем табы к состоянию пейджера
        PrimaryTabRow(
            selectedTabIndex = daysPagerState.currentPage,
            containerColor = Color.Transparent,
            divider = {}, // Убираем нижнюю линию на всю ширину для чистоты
        ) {
            days.forEachIndexed { index, title ->
                Tab(
                    selected = daysPagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            daysPagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (daysPagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { showAddPatientSheet = true },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = "+ Записать пациента",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Ваш список",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Вложенный Pager для списков пациентов
        HorizontalPager(
            state = daysPagerState,
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) { page ->
            // Генерируем или фильтруем расписание для конкретного дня (page)
            val scheduleForDay = getScheduleForDay(page)

            if (scheduleForDay.isEmpty()) {
                EmptyScheduleState()
            } else {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                ) {
                    scheduleForDay.forEach { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            onClick = { selectedAppointment = appointment },
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showAddPatientSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddPatientSheet = false },
            sheetState = sheetState,
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

// Вынесенная логика расписания
fun getScheduleForDay(dayIndex: Int): List<Appointment> {
    return if (dayIndex == 0) {
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
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = appointment.time,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.defaultMinSize(minWidth = 72.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
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
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                    )
                }
            }

            // M3 Цвета для статусов
            val containerColor = if (appointment.isCompleted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
            val contentColor = if (appointment.isCompleted) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer

            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(containerColor)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                Text(
                    text = appointment.status,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = contentColor,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

            // Поле даты теперь только для чтения, клик открывает календарь
            OutlinedTextField(
                value = appointmentDate,
                onValueChange = { },
                label = { Text("Дата приема") },
                placeholder = { Text("Выберите дату") },
                readOnly = true, // Убрали маску, полагаемся только на DatePicker
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                // Клик по всему полю открывает календарь
                shape = RoundedCornerShape(12.dp),
                enabled = false, // Визуальный хак, чтобы клик отрабатывал на Box/Surface лучше, но в M3 OutlinedTextField readOnly работает отлично
                colors =
                    OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Время приема", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            times.chunked(4).forEach { rowTimes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowTimes.forEach { time ->
                        val isSelected = selectedTime == time
                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        },
                                    ).clickable { selectedTime = time }
                                    .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = time,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Тип приема", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            types.chunked(2).forEach { rowTypes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowTypes.forEach { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        },
                                    ).clickable { selectedType = type }
                                    .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = type,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onClose,
            enabled = patientName.isNotEmpty() && appointmentDate.isNotEmpty(),
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
                .padding(horizontal = 24.dp)
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
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
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
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
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
                textAlign = TextAlign.Center,
            )
        }
    }
}
