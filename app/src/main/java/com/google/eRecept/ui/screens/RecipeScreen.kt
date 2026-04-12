package com.google.eRecept.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties
import java.util.UUID

data class MedicationEntry(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var dosage: String = "20 мг",
)

data class RecipeHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val patientName: String,
    val date: String,
    val medications: List<String>,
    val isActive: Boolean
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeScreen() {
    var showCreateSheet by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var selectedRecipe by remember { mutableStateOf<RecipeHistoryItem?>(null) }

    var patientQuery by remember { mutableStateOf("") }
    var medications by remember { mutableStateOf(listOf(MedicationEntry())) }
    var notes by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

    val mockPatients = listOf(
        "Қазыбек Нұрым Байболсынұлы",
        "Қазыбек Ерасыл Арманұлы",
        "Қазыбек Данияр Маратұлы",
        "Азамат Азаматов",
        "Иванов Иван",
    ).filter { it.contains(patientQuery, ignoreCase = true) }

    val mockMeds = listOf(
        "Омепразол (Тева)",
        "Омепразол (Акрихин)",
        "Омепразол (Ozon)",
        "Пенициллин",
        "Аспирин",
    )
    val dosages = listOf("20 мг", "35 мг", "40 мг", "50 мг", "75 мг", "100 мг", "120 мг", "200 мг")

    val dummyHistory = listOf(
        RecipeHistoryItem(patientName = "Қазыбек Нұрым Байболсынұлы", date = "12 апреля 2024", medications = listOf("Аспирин", "Парацетамол"), isActive = true),
        RecipeHistoryItem(patientName = "Иванова Анна", date = "05 марта 2024", medications = listOf("Амоксициллин"), isActive = false),
        RecipeHistoryItem(patientName = "Смирнов Петр", date = "20 февраля 2024", medications = listOf("Ибупрофен", "Витамин С", "Сироп от кашля"), isActive = false),
    )

    val resetForm = {
        patientQuery = ""
        medications = listOf(MedicationEntry())
        notes = ""
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Создать") },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Рецепты",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "История рецептов",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
            ) {
                items(dummyHistory) { recipe ->
                    RecipeHistoryCard(recipe = recipe, onClick = { selectedRecipe = recipe })
                }
            }
        }
    }

    if (selectedRecipe != null) {
        RecipeDetailsDialog(recipe = selectedRecipe!!, onDismiss = { selectedRecipe = null })
    }

    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            var patientExpanded by remember { mutableStateOf(false) }
            var patientTextFieldSize by remember { mutableStateOf(Size.Zero) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "Новый рецепт",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box {
                    OutlinedTextField(
                        value = patientQuery,
                        onValueChange = {
                            patientQuery = it
                            patientExpanded = true
                        },
                        label = { Text("Пациент") },
                        placeholder = { Text("Поиск по ФИО") },
                        trailingIcon = {
                            if (patientQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    patientQuery = ""
                                    patientExpanded = false
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Очистить")
                                }
                            } else {
                                Icon(Icons.Default.Search, contentDescription = "Поиск")
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = {
                            patientExpanded = false
                            focusManager.moveFocus(FocusDirection.Next)
                        }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                patientTextFieldSize = coordinates.size.toSize()
                            },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                    )

                    DropdownMenu(
                        expanded = patientExpanded && patientQuery.isNotEmpty() && mockPatients.isNotEmpty(),
                        onDismissRequest = { patientExpanded = false },
                        properties = PopupProperties(focusable = false),
                        modifier = Modifier
                            .width(with(density) { patientTextFieldSize.width.toDp() })
                            .heightIn(max = 240.dp)
                    ) {
                        mockPatients.forEach { patientName ->
                            DropdownMenuItem(
                                text = { Text(patientName) },
                                onClick = {
                                    patientQuery = patientName
                                    patientExpanded = false
                                    focusManager.moveFocus(FocusDirection.Next)
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                medications.forEachIndexed { index, med ->
                    MedicationItem(
                        index = index,
                        medication = med,
                        mockMeds = mockMeds,
                        dosages = dosages,
                        onMedicationChange = { updatedMed ->
                            medications = medications.map { if (it.id == updatedMed.id) updatedMed else it }
                        },
                        onRemove = if (medications.size > 1) {
                            { medications = medications.filterNot { it.id == med.id } }
                        } else null,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedButton(
                    onClick = { medications = medications + MedicationEntry() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Добавить лекарство")
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Рекомендации") },
                    placeholder = { Text("Особенности приема...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        showCreateSheet = false
                        resetForm()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Готово", style = MaterialTheme.typography.titleMedium)
                }

                TextButton(
                    onClick = { showCancelDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text("Отмена", color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Сбросить черновик?") },
            text = { Text("Введенные данные не сохранятся.") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    showCreateSheet = false
                    resetForm()
                }) {
                    Text("Да", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Нет")
                }
            },
        )
    }
}

@Composable
fun RecipeHistoryCard(recipe: RecipeHistoryItem, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.patientName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = recipe.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.medications.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
            
            Badge(
                containerColor = if (recipe.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                contentColor = if (recipe.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = if (recipe.isActive) "Активен" else "Истек",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun RecipeDetailsDialog(recipe: RecipeHistoryItem, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) { Text("Понятно") }
        },
        title = { Text("Рецепт") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                ListItem(
                    headlineContent = { Text(recipe.patientName) },
                    overlineContent = { Text("Пациент") },
                    supportingContent = { Text(recipe.date) }
                )
                HorizontalDivider()
                recipe.medications.forEach { med ->
                    ListItem(
                        headlineContent = { Text(med) },
                        leadingContent = { Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicationItem(
    index: Int,
    medication: MedicationEntry,
    mockMeds: List<String>,
    dosages: List<String>,
    onMedicationChange: (MedicationEntry) -> Unit,
    onRemove: (() -> Unit)?,
) {
    var medExpanded by remember { mutableStateOf(false) }
    var medTextFieldSize by remember { mutableStateOf(Size.Zero) }
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

    val filteredMeds = mockMeds.filter { it.contains(medication.name, ignoreCase = true) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Лекарство ${index + 1}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                if (onRemove != null) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Удалить")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box {
                OutlinedTextField(
                    value = medication.name,
                    onValueChange = {
                        onMedicationChange(medication.copy(name = it))
                        medExpanded = true
                    },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth().onGloballyPositioned { medTextFieldSize = it.size.toSize() },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                )

                DropdownMenu(
                    expanded = medExpanded && medication.name.isNotEmpty() && filteredMeds.isNotEmpty(),
                    onDismissRequest = { medExpanded = false },
                    properties = PopupProperties(focusable = false),
                    modifier = Modifier.width(with(density) { medTextFieldSize.width.toDp() })
                ) {
                    filteredMeds.forEach { medName ->
                        DropdownMenuItem(
                            text = { Text(medName) },
                            onClick = {
                                onMedicationChange(medication.copy(name = medName))
                                medExpanded = false
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Дозировка", style = MaterialTheme.typography.labelSmall)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dosages.forEach { dosage ->
                    FilterChip(
                        selected = medication.dosage == dosage,
                        onClick = { onMedicationChange(medication.copy(dosage = dosage)) },
                        label = { Text(dosage) }
                    )
                }
            }
        }
    }
}
