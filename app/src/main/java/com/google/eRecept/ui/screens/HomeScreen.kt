package com.google.eRecept.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.eRecept.data.Appointment
import com.google.eRecept.ui.viewmodels.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onProfileClick: () -> Unit = {},
    onCreateRecipeClick: (String) -> Unit = {},
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
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddPatientSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            },
        ) { paddingValues ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp),
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Расписание",
                    style =
                        MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                        ),
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Spacer(modifier = Modifier.height(20.dp))

                TabRow(
                    selectedTabIndex = daysPagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        if (daysPagerState.currentPage < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[daysPagerState.currentPage]),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
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
                                    style =
                                        MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            fontWeight = if (daysPagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal,
                                        ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalPager(
                    state = daysPagerState,
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top,
                ) { page ->
                    val pageDateStr =
                        remember(page) {
                            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DAY_OF_YEAR, page)
                            sdf.format(cal.time)
                        }

                    val scheduleForDay = appointments.filter { it.date == pageDateStr }

                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (scheduleForDay.isEmpty()) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState()),
                            ) {
                                EmptyScheduleState()
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 88.dp),
                            ) {
                                items(scheduleForDay, key = { it.id }) { appointment ->
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
    }

    if (showAddPatientSheet) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {
                showAddPatientSheet = false
                viewModel.clearSearchResult()
                focusManager.clearFocus()
            },
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        ) {
            val view = LocalView.current
            LaunchedEffect(view) {
                (view.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window?.setSoftInputMode(
                    android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING,
                )
            }
            AddPatientBottomSheetContent(
                initialPage = daysPagerState.currentPage,
                onClose = {
                    showAddPatientSheet = false
                    viewModel.clearSearchResult()
                    focusManager.clearFocus()
                },
                viewModel = viewModel,
            )
        }
    }

    if (selectedAppointment != null) {
        val currentAppointment = appointments.find { it.id == selectedAppointment!!.id } ?: selectedAppointment!!

        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = { selectedAppointment = null },
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        ) {
            val view = LocalView.current
            LaunchedEffect(view) {
                (view.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window?.setSoftInputMode(
                    android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING,
                )
            }
            AppointmentDetailsBottomSheetContent(
                appointment = currentAppointment,
                onSave = { newStatus ->
                    viewModel.changeAppointmentStatus(currentAppointment, newStatus)
                    selectedAppointment = null
                },
                onCreateRecipeClick = {
                    selectedAppointment = null
                    onCreateRecipeClick(currentAppointment.patient_iin)
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

            val containerColor =
                when (appointment.status) {
                    "Состоялась" -> MaterialTheme.colorScheme.secondaryContainer
                    "Не явился", "Отменена" -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.primaryContainer
                }
            val contentColor =
                when (appointment.status) {
                    "Состоялась" -> MaterialTheme.colorScheme.onSecondaryContainer
                    "Не явился", "Отменена" -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                }

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
fun AddPatientBottomSheetContent(
    initialPage: Int,
    onClose: () -> Unit,
    viewModel: HomeViewModel,
) {
    val focusManager = LocalFocusManager.current
    var iin by remember { mutableStateOf("") }

    val patientResult by viewModel.searchPatientResult.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    var showNotFoundError by remember { mutableStateOf(false) }

    var appointmentDate by remember {
        mutableStateOf(
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(
                Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, initialPage) }.time,
            ),
        )
    }
    var selectedTime by remember { mutableStateOf("") }

    val times = listOf("09:00", "09:20", "09:40", "10:00", "10:20", "10:40", "11:00", "11:20", "14:00", "14:20", "14:40", "15:00")

    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState =
        rememberDatePickerState(
            selectableDates =
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val todayStart =
                            Calendar
                                .getInstance(TimeZone.getTimeZone("UTC"))
                                .apply {
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                        return utcTimeMillis >= todayStart
                    }
                },
        )

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

    LaunchedEffect(iin, isSearching, patientResult) {
        if (iin.length == 12 && !isSearching && patientResult == null) {
            delay(200)
            showNotFoundError = true
        } else {
            showNotFoundError = false
        }
    }

    LaunchedEffect(iin) {
        if (iin.length == 12) {
            viewModel.searchPatient(iin)
        } else {
            viewModel.clearSearchResult()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }.navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

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
            },
        )

        AnimatedVisibility(visible = showNotFoundError) {
            Text(
                text = "Пациент не найден",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }

        AnimatedVisibility(visible = patientResult != null && iin.length == 12) {
            Column {
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
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    enabled = false,
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
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                patientResult?.let {
                    viewModel.addAppointment(it, appointmentDate, selectedTime)
                }
                onClose()
            },
            enabled = patientResult != null && appointmentDate.isNotEmpty() && selectedTime.isNotEmpty(),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("Создать запись")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailsBottomSheetContent(
    appointment: Appointment,
    onSave: (String) -> Unit,
    onCreateRecipeClick: () -> Unit,
) {
    val statuses = listOf("Запланирована", "Состоялась", "Не явился", "Отменена")
    var selectedStatus by remember(appointment.status) { mutableStateOf(appointment.status) }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

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
            text = "ИИН: ${appointment.patient_iin}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${appointment.age} · Пол: ${appointment.gender}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Статус приема",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Новый вертикальный список выбора статусов
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            statuses.forEach { status ->
                val isSelected = selectedStatus == status
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp),
                            ).background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                } else {
                                    Color.Transparent
                                },
                            ).clickable { selectedStatus = status }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = status,
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            ),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Информация:",
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
            onClick = { onSave(selectedStatus) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("Сохранить", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(12.dp))

        FilledTonalButton(
            onClick = onCreateRecipeClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("Выписать рецепт", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun EmptyScheduleState() {
    Box(
        modifier =
            Modifier
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
