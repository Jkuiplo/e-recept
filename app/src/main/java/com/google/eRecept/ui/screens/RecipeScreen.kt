package com.google.eRecept.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.eRecept.data.Medication
import com.google.eRecept.data.MedicationItem
import com.google.eRecept.data.Recipe
import com.google.eRecept.ui.viewmodels.HomeViewModel
import com.google.eRecept.ui.viewmodels.RecipeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    viewModel: RecipeViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel() // Используем для поиска пациента
) {
    var showCreateSheet by remember { mutableStateOf(false) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    val focusManager = LocalFocusManager.current

    val recipes by viewModel.recipes.collectAsStateWithLifecycle()

    // Form state
    var patientIin by remember { mutableStateOf("") }
    val patientResult by homeViewModel.searchPatientResult.collectAsStateWithLifecycle()
    val isSearchingPatient by homeViewModel.isSearching.collectAsStateWithLifecycle()
    
    var medications by remember { mutableStateOf(listOf(MedicationItem())) }
    var notes by remember { mutableStateOf("") }

    val resetForm = {
        patientIin = ""
        medications = listOf(MedicationItem())
        notes = ""
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Создать рецепт")
            }
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
                text = "Рецепты",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(recipes, key = { it.id }) { recipe ->
                    RecipeHistoryCard(recipe = recipe, onClick = { selectedRecipe = recipe })
                }
            }
        }
    }

    if (selectedRecipe != null) {
        RecipeDetailsDialog(
            recipe = selectedRecipe!!, 
            onDismiss = { selectedRecipe = null },
            viewModel = viewModel
        )
    }

    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateSheet = false },
            dragHandle = null,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(bottom = 24.dp),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { showCreateSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                    Text(
                        text = "Новый рецепт",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                    IconButton(onClick = resetForm) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Очистить", tint = MaterialTheme.colorScheme.error)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 20.dp)
                            .verticalScroll(rememberScrollState()),
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = patientIin,
                        onValueChange = {
                            if (it.length <= 12 && it.all { char -> char.isDigit() }) {
                                patientIin = it
                                if (it.length == 12) homeViewModel.searchPatient(it)
                            }
                        },
                        label = { Text("ИИН пациента") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        trailingIcon = {
                            if (isSearchingPatient) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    )

                    if (patientResult != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Пациент: ${patientResult!!.full_name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    } else if (patientIin.length == 12 && !isSearchingPatient) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Пациент не найден",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = "Назначения", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))

                    medications.forEachIndexed { index, med ->
                        SmartMedicationRow(
                            index = index,
                            medication = med,
                            viewModel = viewModel,
                            onMedicationChange = { updatedMed ->
                                medications = medications.toMutableList().also { it[index] = updatedMed }
                            },
                            onRemove = if (medications.size > 1) {
                                { medications = medications.filterIndexed { i, _ -> i != index } }
                            } else null,
                            focusManager = focusManager,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    TextButton(onClick = { medications = medications + MedicationItem() }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Добавить препарат")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Рекомендации") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                        shape = RoundedCornerShape(12.dp),
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                }

                Button(
                    onClick = {
                        viewModel.createRecipe(patientIin, patientResult?.full_name ?: "Неизвестно", medications, notes)
                        showCreateSheet = false
                        resetForm()
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = patientIin.length == 12 && patientResult != null && medications.any { it.name.isNotBlank() },
                ) {
                    Text("Выписать рецепт")
                }
            }
        }
    }
}

@Composable
fun RecipeHistoryCard(recipe: Recipe, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
    val dateStr = sdf.format(Date(recipe.date))

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = recipe.patient_name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                Text(text = "$dateStr • ${recipe.medications.size} препарата", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SmartMedicationRow(
    index: Int,
    medication: MedicationItem,
    viewModel: RecipeViewModel,
    onMedicationChange: (MedicationItem) -> Unit,
    onRemove: (() -> Unit)?,
    focusManager: androidx.compose.ui.focus.FocusManager,
) {
    var nameExpanded by remember { mutableStateOf(false) }
    var dosageExpanded by remember { mutableStateOf(false) }
    var fieldSize by remember { mutableStateOf(Size.Zero) }
    
    val suggestions by viewModel.medicationSuggestions.collectAsStateWithLifecycle()
    
    // Храним выбранное лекарство для подсказок дозировки
    var selectedMedicationData by remember { mutableStateOf<Medication?>(null) }
    
    val density = LocalDensity.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(modifier = Modifier.weight(0.6f)) {
                OutlinedTextField(
                    value = medication.name,
                    onValueChange = { 
                        onMedicationChange(medication.copy(name = it, dosage = ""))
                        viewModel.searchMedications(it)
                        nameExpanded = true
                    },
                    label = { Text("Препарат") },
                    modifier = Modifier.fillMaxWidth().onGloballyPositioned { fieldSize = it.size.toSize() },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
                
                DropdownMenu(
                    expanded = nameExpanded && suggestions.isNotEmpty(),
                    onDismissRequest = { nameExpanded = false },
                    properties = PopupProperties(focusable = false),
                    modifier = Modifier.width(with(density) { fieldSize.width.toDp() })
                ) {
                    suggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text(suggestion.name, fontWeight = FontWeight.Bold)
                                    Text(suggestion.activeSubstance, style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            onClick = {
                                onMedicationChange(medication.copy(name = suggestion.name))
                                selectedMedicationData = suggestion
                                nameExpanded = false
                            }
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(0.4f)) {
                OutlinedTextField(
                    value = medication.dosage,
                    onValueChange = { 
                        onMedicationChange(medication.copy(dosage = it))
                        dosageExpanded = true
                    },
                    label = { Text("Доза") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )

                if (selectedMedicationData != null && selectedMedicationData!!.availableDosages.isNotEmpty()) {
                    DropdownMenu(
                        expanded = dosageExpanded,
                        onDismissRequest = { dosageExpanded = false },
                        properties = PopupProperties(focusable = false),
                    ) {
                        selectedMedicationData!!.availableDosages.forEach { dosage ->
                            DropdownMenuItem(
                                text = { Text(dosage) },
                                onClick = {
                                    onMedicationChange(medication.copy(dosage = dosage))
                                    dosageExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun RecipeDetailsDialog(recipe: Recipe, onDismiss: () -> Unit, viewModel: RecipeViewModel) {
    val qrBitmap = remember(recipe.id) { viewModel.generateQrCode(recipe.id) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } },
        title = { Text("Детали рецепта", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(200.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "QR-код содержит ID рецепта: ${recipe.id}. Аптекарь сканирует его для получения данных из базы.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Пациент: ${recipe.patient_name}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("ИИН: ${recipe.patient_iin}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Назначения:", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                recipe.medications.forEach { med ->
                    Text("• ${med.name} (${med.dosage})", modifier = Modifier.padding(vertical = 4.dp))
                }
                if (recipe.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Примечания:", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Text(recipe.notes)
                }
            }
        }
    )
}
