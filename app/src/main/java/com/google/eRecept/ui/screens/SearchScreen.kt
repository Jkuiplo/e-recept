package com.google.eRecept.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.eRecept.ui.components.CustomSegmentedButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Пациенты", "Препараты")

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sortAscending by rememberSaveable { mutableStateOf(true) }

    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var selectedMedication by remember { mutableStateOf<Medication?>(null) }

    val allPatients = remember {
        listOf(
            Patient(
                name = "Азаматов Азамат",
                age = 45,
                iin = "790101300123",
                visitsCount = 5,
                diseaseHistory = listOf("Гастрит", "Артериальная гипертензия"),
                relevantPrescriptions = listOf("Омепразол", "Лизиноприл")
            ),
            Patient(
                name = "Иванова Анна",
                age = 28,
                iin = "950202400456",
                visitsCount = 1,
                diseaseHistory = listOf("ОРВИ"),
                relevantPrescriptions = listOf("Ибупрофен")
            ),
            Patient(
                name = "Қазыбек Нұрым",
                age = 68,
                iin = "580303300789",
                visitsCount = 12,
                diseaseHistory = listOf("Хронический бронхит", "Ишемическая болезнь сердца"),
                relevantPrescriptions = listOf("Амоксициллин", "Аспирин Кардио")
            ),
            Patient(
                name = "Смирнов Петр",
                age = 34,
                iin = "890404300111",
                visitsCount = 3,
                diseaseHistory = listOf("Остеохондроз"),
                relevantPrescriptions = emptyList()
            ),
        )
    }

    val allMedications = remember {
        listOf(
            Medication(
                name = "Амоксициллин",
                activeSubstance = "Амоксициллин",
                defaultDosage = "500 мг",
                description = "Антибиотик группы полусинтетических пенициллинов широкого спектра действия.",
                indications = "Инфекции дыхательных путей, мочеполовой системы, ЖКТ.",
                sideEffects = "Аллергические реакции, тошнота, диарея."
            ),
            Medication(
                name = "Аспирин",
                activeSubstance = "Ацетилсалициловая кислота",
                defaultDosage = "100 мг",
                description = "Нестероидный противовоспалительный препарат (НПВП).",
                indications = "Лихорадка, болевой синдром, профилактика тромбозов.",
                sideEffects = "Боли в животе, риск кровотечений."
            ),
            Medication(
                name = "Ибупрофен",
                activeSubstance = "Ибупрофен",
                defaultDosage = "400 мг",
                description = "НПВП, производное пропионовой кислоты.",
                indications = "Головная и зубная боль, невралгии, боли в суставах.",
                sideEffects = "Изжога, метеоризм, повышение АД."
            ),
            Medication(
                name = "Омепразол (Тева)",
                activeSubstance = "Омепразол",
                defaultDosage = "20 мг",
                description = "Ингибитор протонной помпы, снижает секрецию желудочного сока.",
                indications = "Язвенная болезнь, гастрит, рефлюкс-эзофагит.",
                sideEffects = "Головная боль, запор или диарея."
            ),
        )
    }

    val displayedPatients = allPatients
        .filter { it.name.contains(searchQuery, ignoreCase = true) || it.iin.contains(searchQuery) }
        .let { if (sortAscending) it.sortedBy { p -> p.name } else it.sortedByDescending { p -> p.name } }

    val displayedMedications = allMedications
        .filter { it.name.contains(searchQuery, ignoreCase = true) }
        .let { if (sortAscending) it.sortedBy { m -> m.name } else it.sortedByDescending { m -> m.name } }

    Column(
        modifier = Modifier
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

        Spacer(modifier = Modifier.height(24.dp))

        CustomSegmentedButton(
            options = tabs,
            selectedIndex = selectedTabIndex,
            onOptionSelected = { selectedTabIndex = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

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
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )

            FilledIconButton(
                onClick = { sortAscending = !sortAscending },
                shape = RoundedCornerShape(12.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                modifier = Modifier.size(56.dp),
            ) {
                Text(
                    text = if (sortAscending) "А-Я" else "Я-А",
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
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
                        onAddToRecipe = { /* TODO */ },
                    )
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
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = patient.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${patient.age} лет • ИИН: ${patient.iin}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier
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
                Text(text = "Дозировка: ${medication.defaultDosage}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            TextButton(
                onClick = onAddToRecipe,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Добавить", fontWeight = FontWeight.Bold)
            }
        }
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(text = patient.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = "ИИН: ${patient.iin}", color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(24.dp))

            Text("История болезней", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            patient.diseaseHistory.forEach { disease ->
                Text("• $disease", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Актуальные рецепты", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            if (patient.relevantPrescriptions.isEmpty()) {
                Text("Актуальных рецептов нет", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                patient.relevantPrescriptions.forEach { med ->
                    Text("• $med", modifier = Modifier.padding(vertical = 4.dp), style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onBookAppointment,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(text = medication.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = medication.activeSubstance, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
            
            Spacer(modifier = Modifier.height(24.dp))

            Text("Описание", fontWeight = FontWeight.Bold)
            Text(medication.description)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Показания к применению", fontWeight = FontWeight.Bold)
            Text(medication.indications)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Побочные эффекты", fontWeight = FontWeight.Bold)
            Text(medication.sideEffects)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Добавить к рецепту", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}
