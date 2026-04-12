package com.google.eRecept.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.UUID

data class Patient(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val age: Int,
    val iin: String,
    val visitsCount: Int,
    val diseaseHistory: List<String>,
    val relevantPrescriptions: List<String>,
)

data class Medication(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val activeSubstance: String,
    val defaultDosage: String,
    val description: String,
    val indications: String,
    val sideEffects: String,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen() {
    val tabs = listOf("Пациенты", "Препараты", "Рецепты")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sortAscending by rememberSaveable { mutableStateOf(true) }

    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var selectedMedication by remember { mutableStateOf<Medication?>(null) }

    val allPatients =
        remember {
            listOf(
                Patient(
                    name = "Азаматов Азамат",
                    age = 45,
                    iin = "790101300123",
                    visitsCount = 5,
                    diseaseHistory = listOf("Гастрит", "Артериальная гипертензия"),
                    relevantPrescriptions = listOf("Омепразол", "Лизиноприл"),
                ),
                Patient(
                    name = "Иванова Анна",
                    age = 28,
                    iin = "950202400456",
                    visitsCount = 1,
                    diseaseHistory = listOf("ОРВИ"),
                    relevantPrescriptions = listOf("Ибупрофен"),
                ),
                Patient(
                    name = "Қазыбек Нұрым",
                    age = 68,
                    iin = "580303300789",
                    visitsCount = 12,
                    diseaseHistory = listOf("Хронический бронхит", "Ишемическая болезнь сердца"),
                    relevantPrescriptions = listOf("Амоксициллин", "Аспирин Кардио"),
                ),
                Patient(
                    name = "Смирнов Петр",
                    age = 34,
                    iin = "890404300111",
                    visitsCount = 3,
                    diseaseHistory = listOf("Остеохондроз"),
                    relevantPrescriptions = emptyList(),
                ),
            )
        }

    val allMedications =
        remember {
            listOf(
                Medication(
                    name = "Амоксициллин",
                    activeSubstance = "Амоксициллин",
                    defaultDosage = "500 мг",
                    description = "Антибиотик группы полусинтетических пенициллинов широкого спектра действия.",
                    indications = "Инфекции дыхательных путей, мочеполовой системы, ЖКТ.",
                    sideEffects = "Аллергические реакции, тошнота, диарея.",
                ),
                Medication(
                    name = "Аспирин",
                    activeSubstance = "Ацетилсалициловая кислота",
                    defaultDosage = "100 мг",
                    description = "Нестероидный противовоспалительный препарат (НПВП).",
                    indications = "Лихорадка, болевой синдром, профилактика тромбозов.",
                    sideEffects = "Боли в животе, риск кровотечений.",
                ),
                Medication(
                    name = "Ибупрофен",
                    activeSubstance = "Ибупрофен",
                    defaultDosage = "400 мг",
                    description = "НПВП, производное пропионовой кислоты.",
                    indications = "Головная и зубная боль, невралгии, боли в суставах.",
                    sideEffects = "Изжога, метеоризм, повышение АД.",
                ),
                Medication(
                    name = "Омепразол (Тева)",
                    activeSubstance = "Омепразол",
                    defaultDosage = "20 мг",
                    description = "Ингибитор протонной помпы, снижает секрецию желудочного сока.",
                    indications = "Язвенная болезнь, гастрит, рефлюкс-эзофагит.",
                    sideEffects = "Головная боль, запор или диарея.",
                ),
            )
        }

    // Мемоизация сортировки и фильтрации: пересчитываем ТОЛЬКО если изменился поиск или тип сортировки
    val displayedPatients =
        remember(searchQuery, sortAscending) {
            allPatients
                .filter { it.name.contains(searchQuery, ignoreCase = true) || it.iin.contains(searchQuery) }
                .let { if (sortAscending) it.sortedBy { p -> p.name } else it.sortedByDescending { p -> p.name } }
        }

    val displayedMedications =
        remember(searchQuery, sortAscending) {
            allMedications
                .filter { it.name.contains(searchQuery, ignoreCase = true) }
                .let { if (sortAscending) it.sortedBy { m -> m.name } else it.sortedByDescending { m -> m.name } }
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Поиск",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Поле поиска поднято выше табов. Использован легкий OutlinedTextField вместо тяжелого SearchBar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text =
                            when (pagerState.currentPage) {
                                0 -> "Поиск по ФИО или ИИН"
                                1 -> "Поиск препарата"
                                else -> "Поиск рецепта"
                            },
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                shape = CircleShape,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )

            IconButton(
                onClick = { sortAscending = !sortAscending },
                modifier =
                    Modifier.background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        CircleShape,
                    ),
            ) {
                Icon(
                    imageVector = if (sortAscending) Icons.Default.SortByAlpha else Icons.Default.Sort,
                    contentDescription = "Sort",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            divider = {},
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(title) },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
        ) { page ->
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                when (page) {
                    0 -> {
                        // ДОБАВЛЕН key = { it.id } для устранения лагов скролла
                        items(displayedPatients, key = { it.id }) { patient ->
                            PatientListItem(patient = patient, onClick = { selectedPatient = patient })
                        }
                    }

                    1 -> {
                        // ДОБАВЛЕН key = { it.id }
                        items(displayedMedications, key = { it.id }) { med ->
                            MedicationListItem(
                                medication = med,
                                onClick = { selectedMedication = med },
                                onAddToRecipe = { /* TODO */ },
                            )
                        }
                    }

                    2 -> {
                        item {
                            Box(
                                modifier =
                                    Modifier
                                        .fillParentMaxSize()
                                        .padding(top = 40.dp),
                                contentAlignment = Alignment.TopCenter,
                            ) {
                                Text(
                                    text = "Здесь будет поиск по выписанным рецептам",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedPatient?.let { patient ->
        PatientProfileSheet(
            patient = patient,
            onDismiss = { selectedPatient = null },
            onBookAppointment = { selectedPatient = null },
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
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        ListItem(
            headlineContent = { Text(patient.name, fontWeight = FontWeight.Bold) },
            supportingContent = { Text("${patient.age} лет • ИИН: ${patient.iin}") },
            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
fun MedicationListItem(
    medication: Medication,
    onClick: () -> Unit,
    onAddToRecipe: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = medication.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            supportingContent = { Text("Дозировка: ${medication.defaultDosage}") },
            trailingContent = {
                IconButton(onClick = onAddToRecipe) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
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
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(text = patient.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = "ИИН: ${patient.iin}", color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(24.dp))

            Text("История болезней", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            patient.diseaseHistory.forEach { disease ->
                ListItem(
                    headlineContent = { Text(disease) },
                    leadingContent = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(20.dp)) },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Актуальные рецепты", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            if (patient.relevantPrescriptions.isEmpty()) {
                Text("Актуальных рецептов нет", modifier = Modifier.padding(8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                patient.relevantPrescriptions.forEach { med ->
                    ListItem(
                        headlineContent = { Text(med) },
                        leadingContent = {
                            Icon(
                                Icons.Default.MedicalServices,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onBookAppointment,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Записать на прием", style = MaterialTheme.typography.titleMedium)
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
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(text = medication.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = medication.activeSubstance, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Описание", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(medication.description, style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Показания", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(medication.indications, style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Побочные эффекты", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                    Text(medication.sideEffects, style = MaterialTheme.typography.bodyMedium)
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
                Text("Добавить к рецепту", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
