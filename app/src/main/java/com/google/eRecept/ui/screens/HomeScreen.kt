package com.google.eRecept.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.eRecept.ui.components.CustomSegmentedButton
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

    var selectedDayIndex by remember { mutableStateOf(0) }
    val days = listOf("Сегодня", "Завтра", "Послезавтра")

    val schedule = remember(selectedDayIndex) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Главная",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(24.dp))

        CustomSegmentedButton(
            options = days,
            selectedIndex = selectedDayIndex,
            onOptionSelected = { selectedDayIndex = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { showAddPatientSheet = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
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
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
            ) {
                schedule.forEach { appointment ->
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
                    maxLines = 1
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
                        maxLines = 1
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            val badgeColor = if (appointment.isCompleted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
            val badgeBg = badgeColor.copy(alpha = 0.15f)

            Box(
                modifier = Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientBottomSheetContent(onClose: () -> Unit) {
    var iin by remember { mutableStateOf("") }
    var patientName by remember { mutableStateOf("") }
    var patientAge by remember { mutableStateOf("") }
    var patientGender by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    
    var appointmentDate by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Первичный") }
    val types = listOf("Первичный", "Повторный", "Консультация", "Осмотр")

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
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 40.dp)
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
            shape = RoundedCornerShape(12.dp)
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
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = patientAge,
                    onValueChange = {},
                    label = { Text("Возраст") },
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = patientGender,
                    onValueChange = {},
                    label = { Text("Пол") },
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = appointmentDate,
                onValueChange = { if (it.length <= 8) appointmentDate = it },
                label = { Text("Дата приема") },
                placeholder = { Text("ДД.ММ.ГГГГ") },
                visualTransformation = DateTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Тип приема", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            
            types.chunked(2).forEach { rowTypes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowTypes.forEach { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedType = type }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
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
            enabled = patientName.isNotEmpty() && appointmentDate.length == 8,
            modifier = Modifier
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
        modifier = Modifier
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
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (appointment.allergy != null) FontWeight.Bold else FontWeight.Normal,
            ),
            color = if (appointment.allergy != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCreateRecipeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
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
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = onAddPatientClick) {
            Text("Записать пациента", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}
