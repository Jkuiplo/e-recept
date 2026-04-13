package com.google.eRecept.ui.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.eRecept.data.Medication
import com.google.eRecept.data.Patient
import com.google.eRecept.data.Recipe
import com.google.eRecept.ui.viewmodels.SearchViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel) {
    val tabs = listOf("Пациенты", "Препараты", "История")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    
    val patientResults by viewModel.patientResults.collectAsStateWithLifecycle()
    val medicationResults by viewModel.medicationResults.collectAsStateWithLifecycle()
    val allRecipes by viewModel.allRecipes.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var selectedMedication by remember { mutableStateOf<Medication?>(null) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }

    LaunchedEffect(searchQuery, pagerState.currentPage) {
        viewModel.search(searchQuery, pagerState.currentPage)
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

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = when(pagerState.currentPage) {
                        0 -> "ФИО или ИИН пациента"
                        1 -> "Название препарата"
                        else -> "Поиск в истории"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else if (searchQuery.isNotEmpty()) {
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
            modifier = Modifier.fillMaxWidth(),
        )

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
                        if (patientResults.isEmpty() && searchQuery.isNotEmpty() && !isSearching) {
                            item { EmptyState("Пациенты не найдены") }
                        }
                        items(patientResults, key = { it.iin }) { patient ->
                            PatientListItem(patient = patient, onClick = { selectedPatient = patient })
                        }
                    }

                    1 -> {
                        if (medicationResults.isEmpty() && searchQuery.isNotEmpty() && !isSearching) {
                            item { EmptyState("Препараты не найдены") }
                        }
                        items(medicationResults, key = { it.id }) { medication ->
                            MedicationListItem(medication = medication, onClick = { selectedMedication = medication })
                        }
                    }

                    2 -> {
                        val filteredRecipes = if (searchQuery.isBlank()) allRecipes else {
                            allRecipes.filter { 
                                it.patient_name.contains(searchQuery, ignoreCase = true) || 
                                it.patient_iin.contains(searchQuery) 
                            }
                        }
                        items(filteredRecipes, key = { it.id }) { recipe ->
                            RecipeHistoryCard(recipe = recipe, onClick = { selectedRecipe = recipe })
                        }
                    }
                }
            }
        }
    }

    if (selectedPatient != null) {
        PatientProfileSheet(patient = selectedPatient!!, onDismiss = { selectedPatient = null })
    }

    if (selectedMedication != null) {
        MedicationInfoSheet(medication = selectedMedication!!, onDismiss = { selectedMedication = null })
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PatientListItem(patient: Patient, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        ListItem(
            headlineContent = { Text(patient.full_name, fontWeight = FontWeight.Bold) },
            supportingContent = { Text("ИИН: ${patient.iin}") },
            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
fun MedicationListItem(medication: Medication, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        ListItem(
            headlineContent = { Text(medication.name, fontWeight = FontWeight.Bold) },
            supportingContent = { Text(medication.activeSubstance) },
            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileSheet(patient: Patient, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState()),
        ) {
            Text(text = patient.full_name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = "ИИН: ${patient.iin}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Пол: ${patient.gender} • Рождение: ${patient.birth_date}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Аллергии", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(text = patient.allergies.ifEmpty { "Не указано" }, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Закрыть")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationInfoSheet(medication: Medication, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState()),
        ) {
            Text(text = medication.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = medication.activeSubstance, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Описание", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(medication.description.ifEmpty { "Нет описания" })
            Spacer(modifier = Modifier.height(16.dp))
            Text("Дозировки", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(medication.availableDosages.joinToString(", ").ifEmpty { "Не указаны" })
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Закрыть")
            }
        }
    }
}
