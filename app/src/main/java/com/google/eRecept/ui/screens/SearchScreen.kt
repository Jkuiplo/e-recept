package com.google.eRecept.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

// --- Моковые данные ---
data class Patient(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val age: Int,
    val iin: String,
    val visitsCount: Int,
    val lastPrescriptions: List<String>,
)

data class Medication(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val activeSubstance: String,
    val defaultDosage: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    // 0 = Пациенты, 1 = Препараты. rememberSaveable сохраняет состояние при перевороте экрана
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Пациенты", "Препараты")

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sortAscending by rememberSaveable { mutableStateOf(true) } // Сортировка А-Я / Я-А

    // Состояния для открытия BottomSheets
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var selectedMedication by remember { mutableStateOf<Medication?>(null) }

    // Базовые списки
    val allPatients =
        remember {
            listOf(
                Patient(
                    name = "Азаматов Азамат",
                    age = 45,
                    iin = "790101300123",
                    visitsCount = 5,
                    lastPrescriptions = listOf("Аспирин", "Омепразол"),
                ),
                Patient(name = "Иванова Анна", age = 28, iin = "950202400456", visitsCount = 1, lastPrescriptions = listOf("Ибупрофен")),
                Patient(
                    name = "Қазыбек Нұрым",
                    age = 68,
                    iin = "580303300789",
                    visitsCount = 12,
                    lastPrescriptions = listOf("Пенициллин", "Парацетамол"),
                ),
                Patient(name = "Смирнов Петр", age = 34, iin = "890404300111", visitsCount = 3, lastPrescriptions = emptyList()),
            )
        }

    val allMedications =
        remember {
            listOf(
                Medication(name = "Амоксициллин", activeSubstance = "Амоксициллин", defaultDosage = "500 мг"),
                Medication(name = "Аспирин", activeSubstance = "Ацетилсалициловая кислота", defaultDosage = "100 мг"),
                Medication(name = "Ибупрофен", activeSubstance = "Ибупрофен", defaultDosage = "400 мг"),
                Medication(name = "Омепразол (Тева)", activeSubstance = "Омепразол", defaultDosage = "20 мг"),
                Medication(name = "Пенициллин", activeSubstance = "Бензилпенициллин", defaultDosage = "1 млн ЕД"),
            )
        }

    // Фильтрация и сортировка на лету
    val displayedPatients =
        allPatients
            .filter { it.name.contains(searchQuery, ignoreCase = true) || it.iin.contains(searchQuery) }
            .let { if (sortAscending) it.sortedBy { p -> p.name } else it.sortedByDescending { p -> p.name } }

    val displayedMedications =
        allMedications
            .filter { it.name.contains(searchQuery, ignoreCase = true) }
            .let { if (sortAscending) it.sortedBy { m -> m.name } else it.sortedByDescending { m -> m.name } }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // Динамический фон
                .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Поиск",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Сегментированные кнопки (Patients / Medications) ---
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            tabs.forEachIndexed { index, title ->
                SegmentedButton(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        searchQuery = "" // Очищаем поиск при переключении вкладки
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
                    colors =
                        SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            inactiveContainerColor = MaterialTheme.colorScheme.surface,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                ) {
                    Text(title, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Строка поиска и фильтров ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(if (selectedTabIndex == 0) "Поиск по ФИО или ИИН" else "Поиск препарата") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                singleLine = true,
            )

            // Кнопка сортировки (А-Я / Я-А)
            FilledIconButton(
                onClick = { sortAscending = !sortAscending },
                shape = RoundedCornerShape(16.dp),
                colors =
                    IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                modifier = Modifier.size(56.dp),
            ) {
                // Здесь в идеале нужна иконка Sort. Для примера используем текст.
                Text(
                    text = if (sortAscending) "А-Я" else "Я-А",
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Списки (динамически меняются при вводе) ---
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (selectedTabIndex == 0) {
                items(displayedPatients) { patient ->
                    PatientListItem(patient = patient, onClick = { selectedPatient = patient })
                }
            } else {
                items(displayedMedications) { med ->
                    MedicationListItem(
                        medication = med,
                        onClick = { selectedMedication = med },
                        onAddToRecipe = { /* TODO: Логика добавления в рецепт */ },
                    )
                }
            }
        }
    }

    // --- Bottom Sheets ---
    selectedPatient?.let { patient ->
        PatientProfileSheet(
            patient = patient,
            onDismiss = { selectedPatient = null },
            onBookAppointment = {
                // TODO: Вызов второй модалки записи. Пока просто закроем эту.
                selectedPatient = null
            },
        )
    }

    selectedMedication?.let { med ->
        MedicationInfoSheet(
            medication = med,
            onDismiss = { selectedMedication = null },
        )
    }
}

@Composable
fun PatientListItem(
    patient: Patient,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant, // Динамический цвет карточки
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleAvatar(initials = patient.name.take(1).uppercase())
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = patient.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "${patient.age} лет • ИИН: ${patient.iin}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun MedicationListItem(
    medication: Medication,
    onClick: () -> Unit,
    onAddToRecipe: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(text = "Дозировка: ${medication.defaultDosage}", style = MaterialTheme.typography.bodyMedium)
            }

            // Кнопка быстрого добавления в рецепт
            IconButton(
                onClick = onAddToRecipe,
                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить в рецепт")
            }
        }
    }
}

@Composable
fun CircleAvatar(initials: String) {
    Box(
        modifier =
            Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(100.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileSheet(
    patient: Patient,
    onDismiss: () -> Unit,
    onBookAppointment: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircleAvatar(initials = patient.name.take(1).uppercase())
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = patient.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(text = "ИИН: ${patient.iin}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Блок медицинской инфы
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Медицинское досье", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Возраст: ${patient.age} лет")
                    Text("Количество приемов: ${patient.visitsCount}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Блок истории рецептов
            Text("История рецептов", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (patient.lastPrescriptions.isEmpty()) {
                Text("Рецепты еще не выписывались", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                patient.lastPrescriptions.forEach { med ->
                    Text("• $med", modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Главная кнопка "Записать"
            Button(
                onClick = onBookAppointment,
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
                Text("Записать на прием", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationInfoSheet(
    medication: Medication,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
        ) {
            Text(text = medication.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Действующее вещество:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(medication.activeSubstance, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Стандартная дозировка:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(medication.defaultDosage, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { /* TODO */ },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Добавить к рецепту", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}
