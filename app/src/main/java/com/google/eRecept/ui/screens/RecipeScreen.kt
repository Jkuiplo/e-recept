package com.google.eRecept.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
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
    homeViewModel: HomeViewModel = viewModel(),
) {
    val focusManager = LocalFocusManager.current
    var showCreateSheet by remember { mutableStateOf(false) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    // Черновик из ViewModel
    val draftPatientIin by viewModel.draftPatientIin.collectAsStateWithLifecycle()
    val draftMedications by viewModel.draftMedications.collectAsStateWithLifecycle()
    val draftNotes by viewModel.draftNotes.collectAsStateWithLifecycle()

    val patientResult by homeViewModel.searchPatientResult.collectAsStateWithLifecycle()
    val isSearchingPatient by homeViewModel.isSearching.collectAsStateWithLifecycle()

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
                    onClick = { showCreateSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
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

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (recipes.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyRecipesState()
                        }
                    } else {
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
            }
        }
    }

    if (selectedRecipe != null) {
        RecipeDetailsDialog(
            recipe = selectedRecipe!!,
            onDismiss = { selectedRecipe = null },
            viewModel = viewModel,
        )
    }

    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxHeight(),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { focusManager.clearFocus() })
                        },
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { showCreateSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                        Text(
                            text = "Новый рецепт",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                        )
                        IconButton(onClick = { viewModel.clearDraft() }) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Очистить черновик",
                                tint = MaterialTheme.colorScheme.error,
                            )
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
                            value = draftPatientIin,
                            onValueChange = {
                                if (it.length <= 12 && it.all { char -> char.isDigit() }) {
                                    viewModel.updateDraftIin(it)
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
                            },
                        )

                        if (patientResult != null && draftPatientIin.length == 12) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Пациент: ${patientResult!!.full_name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp),
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "Назначения",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        draftMedications.forEachIndexed { index, med ->
                            SmartMedicationRow(
                                index = index,
                                medication = med,
                                viewModel = viewModel,
                                onMedicationChange = { updatedMed ->
                                    val newList = draftMedications.toMutableList()
                                    newList[index] = updatedMed
                                    viewModel.updateDraftMedications(newList)
                                },
                                onRemove =
                                    if (draftMedications.size > 1) {
                                        {
                                            val newList = draftMedications.filterIndexed { i, _ -> i != index }
                                            viewModel.updateDraftMedications(newList)
                                        }
                                    } else {
                                        null
                                    },
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        TextButton(onClick = { viewModel.updateDraftMedications(draftMedications + MedicationItem()) }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Добавить препарат")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = draftNotes,
                            onValueChange = { viewModel.updateDraftNotes(it) },
                            label = { Text("Рекомендации") },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                            shape = RoundedCornerShape(12.dp),
                        )

                        Spacer(modifier = Modifier.height(40.dp))
                    }

                    Button(
                        onClick = {
                            viewModel.createRecipe(patientResult?.full_name ?: "Неизвестно")
                            showCreateSheet = false
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = draftPatientIin.length == 12 && patientResult != null && draftMedications.any { it.name.isNotBlank() },
                    ) {
                        Text("Выписать рецепт")
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeHistoryCard(
    recipe: Recipe,
    onClick: () -> Unit,
) {
    val sdf = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
    val dateStr = sdf.format(Date(recipe.date))
    val recipeNum = recipe.id.takeLast(4).uppercase()

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Рецепт №$recipeNum",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = recipe.patient_name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
) {
    var nameExpanded by remember { mutableStateOf(false) }
    var dosageExpanded by remember { mutableStateOf(false) }
    var fieldSize by remember { mutableStateOf(Size.Zero) }
    val suggestions by viewModel.medicationSuggestions.collectAsStateWithLifecycle()
    var selectedMedicationData by remember { mutableStateOf<Medication?>(null) }
    val density = LocalDensity.current

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
                modifier = Modifier.width(with(density) { fieldSize.width.toDp() }),
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
                        },
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
                            },
                        )
                    }
                }
            }
        }

        if (onRemove != null) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun RecipeDetailsDialog(
    recipe: Recipe,
    onDismiss: () -> Unit,
    viewModel: RecipeViewModel,
) {
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
                        contentDescription = null,
                        modifier = Modifier.size(200.dp),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Пациент: ${recipe.patient_name}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("ИИН: ${recipe.patient_iin}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                recipe.medications.forEach { med ->
                    Text("• ${med.name} (${med.dosage})", modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        },
    )
}

@Composable
fun EmptyRecipesState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = 100.dp),
    ) {
        Icon(
            imageVector = Icons.Default.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Рецептов пока нет",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Вы еще не выписали ни одного рецепта.\nНажмите +, чтобы создать первый.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
