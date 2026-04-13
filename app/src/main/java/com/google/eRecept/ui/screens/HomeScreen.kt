package com.google.eRecept.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.eRecept.data.Appointment
import com.google.eRecept.ui.viewmodels.HomeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onProfileClick: () -> Unit = {},
    onCreateRecipeClick: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    var showAddPatientSheet by remember { mutableStateOf(false) }
    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }

    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val days = listOf("Сегодня", "Завтра", "Послезавтра")
    val daysPagerState = rememberPagerState(pageCount = { days.size })
    val coroutineScope = rememberCoroutineScope()

    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, daysPagerState.currentPage)
    val selectedDate = calendar.time
    val dateFormatter = SimpleDateFormat("d MMMM, EEEE", Locale("ru"))
    val formattedDate = dateFormatter.format(selectedDate)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Column(
            modifier = Modifier
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

            PrimaryTabRow(
                selectedTabIndex = daysPagerState.currentPage,
                containerColor = Color.Transparent,
                divider = {},
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
                modifier = Modifier
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

            HorizontalPager(
                state = daysPagerState,
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) { page ->
                val pageDateStr = remember(page) {
                    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, page)
                    sdf.format(cal.time)
                }

                val scheduleForDay = appointments.filter { it.date == pageDateStr }

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (scheduleForDay.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            EmptyScheduleState()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(scheduleForDay, key = { it.time + it.patient_iin }) { appointment ->
                                AppointmentCard(
                                    appointment = appointment,
                                    onClick = { selectedAppointment = appointment },
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddPatientSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddPatientSheet = false
                focusManager.clearFocus()
            }
        ) {
            AddPatientBottomSheetContent(
                onClose = {
                    showAddPatientSheet = false
                    focusManager.clearFocus()
                },
                viewModel = viewModel
            )
        }
    }

    if (selectedAppointment != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedAppointment = null }
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

@Composable
fun AppointmentCard(
    appointment: Appointment,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
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
                    text = appointment.patient_name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    text = "${appointment.age} · ${appointment.gender}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            val containerColor = if (appointment.is_completed) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
            val contentColor = if (appointment.is_completed) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer

            Box(
                modifier = Modifier
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
fun AddPatientBottomSheetContent(onClose: () -> Unit, viewModel: HomeViewModel) {
    val focusManager = LocalFocusManager.current
    var iin by remember { mutableStateOf("") }

    val patientResult by viewModel.searchPatientResult.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

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

    LaunchedEffect(iin) {
        if (iin.length == 12) {
            viewModel.searchPatient(iin)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
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
                }
            },
            label = { Text("ИИН пациента") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
        )

        if (patientResult != null) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = patientResult!!.full_name,
                onValueChange = {},
                label = { Text("ФИО") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = appointmentDate,
                onValueChange = { },
                label = { Text("Дата приема") },
                placeholder = { Text("Выберите дату") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
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
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { selectedTime = time }
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
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { selectedType = type }
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
        } else if (iin.length == 12 && !isSearching) {
            Text(
                text = "Пациент не найден",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                patientResult?.let {
                    viewModel.addAppointment(it, appointmentDate, selectedTime, selectedType)
                }
                onClose()
            },
            enabled = patientResult != null && appointmentDate.isNotEmpty() && selectedTime.isNotEmpty(),
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
            text = appointment.patient_name,
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
            text = "Дополнительно:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = appointment.history.ifEmpty { "Нет данных" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCreateRecipeClick,
            modifier = Modifier
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
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(top = 100.dp),
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