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
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val showCreateSheet by viewModel.showCreateSheet.collectAsStateWithLifecycle()

    // Черновик из ViewModel
    val draftPatientIin by viewModel.draftPatientIin.collectAsStateWithLifecycle()
    val draftMedications by viewModel.draftMedications.collectAsStateWithLifecycle()
    val draftNotes by viewModel.draftNotes.collectAsStateWithLifecycle()

    val patientResult by homeViewModel.searchPatientResult.collectAsStateWithLifecycle()
    val isSearchingPatient by homeViewModel.isSearching.collectAsStateWithLifecycle()

    // Триггерим поиск пациента, если ИИН прилетел из Главной страницы
    LaunchedEffect(draftPatientIin) {
        if (draftPatientIin.length == 12) {
            homeViewModel.searchPatient(draftPatientIin)
        } else if (draftPatientIin.isEmpty()) {
            homeViewModel.clearSearchResult()
        }
    }

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
                    onClick = { viewModel.openCreateSheet() },
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
            onDismissRequest = { viewModel.closeCreateSheet() },
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
                    // Выкинули верхнюю шапку с крестиками

                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 20.dp)
                                .verticalScroll(rememberScrollState()),
                    ) {
                        Text(
                            text = "Выписать рецепт",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = draftPatientIin,
                            onValueChange = {
                                if (it.length <= 12 && it.all { char -> char.isDigit() }) {
                                    viewModel.updateDraftIin(it)
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
                                } else if (draftPatientIin.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateDraftIin("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Очистить")
                                    }
                                }
                            },
                        )

                        if (draftPatientIin.length == 12 && !isSearchingPatient && patientResult == null) {
                            Text(
                                text = "Пациент не найден",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                            )
                        } else if (patientResult != null && draftPatientIin.length == 12) {
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
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        TextButton(
                            onClick = { viewModel.updateDraftMedications(draftMedications + MedicationItem()) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Добавить препарат")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = draftNotes,
                            onValueChange = { viewModel.updateDraftNotes(it) },
                            label = { Text("Общие рекомендации (опционально)") },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                            shape = RoundedCornerShape(12.dp),
                        )

                        Spacer(modifier = Modifier.height(40.dp))
                    }

                    Button(
                        onClick = {
                            viewModel.createRecipe(patientResult?.full_name ?: "Неизвестно")
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                                .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = draftPatientIin.length == 12 && patientResult != null && draftMedications.any { it.name.isNotBlank() },
                    ) {
                        Text("Выписать рецепт", style = MaterialTheme.typography.titleMedium)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartMedicationRow(
    index: Int,
    medication: MedicationItem,
    viewModel: RecipeViewModel,
    onMedicationChange: (MedicationItem) -> Unit,
    onRemove: (() -> Unit)?,
) {
    var nameExpanded by remember { mutableStateOf(false) }
    var fieldSize by remember { mutableStateOf(Size.Zero) }
    val suggestions by viewModel.medicationSuggestions.collectAsStateWithLifecycle()
    val density = LocalDensity.current

    val dosageUnits = listOf("мг", "мл", "таб")
    val frequencies = listOf("1×", "2×", "3×", "4×")
    val durationUnits = listOf("дней", "нед", "мес")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Шапка карточки: Название и корзина
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Препарат ${index + 1}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                if (onRemove != null) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ПОЛЕ ПРЕПАРАТА
            Box {
                OutlinedTextField(
                    value = medication.name,
                    onValueChange = {
                        onMedicationChange(medication.copy(name = it))
                        viewModel.searchMedications(it)
                        nameExpanded = true
                    },
                    label = { Text("Название") },
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
                                nameExpanded = false
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ПОЛЕ ДОЗИРОВКИ
            Text("Дозировка", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = medication.dosageValue,
                    onValueChange = { onMedicationChange(medication.copy(dosageValue = it)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1.2f)) {
                    dosageUnits.forEachIndexed { i, unit ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = i, count = dosageUnits.size),
                            onClick = { onMedicationChange(medication.copy(dosageUnit = unit)) },
                            selected = medication.dosageUnit == unit,
                        ) { Text(unit, style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ПОЛЕ КРАТНОСТИ
            Text("Кратность приёма", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                frequencies.forEachIndexed { i, freq ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = i, count = frequencies.size),
                        onClick = { onMedicationChange(medication.copy(frequency = freq)) },
                        selected = medication.frequency == freq,
                    ) { Text(freq) }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ПОЛЕ ДЛИТЕЛЬНОСТИ
            Text("Длительность", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = medication.durationValue,
                    onValueChange = { onMedicationChange(medication.copy(durationValue = it)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1.2f)) {
                    durationUnits.forEachIndexed { i, unit ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = i, count = durationUnits.size),
                            onClick = { onMedicationChange(medication.copy(durationUnit = unit)) },
                            selected = medication.durationUnit == unit,
                        ) { Text(unit, style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ИТОГО (BOX)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
            ) {
                Column {
                    Text("Итого", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        medication.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
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
                    // Выводим красивый summary
                    Text("• ${med.name}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                    Text("  ${med.summary}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 4.dp))
                }
                if (recipe.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Рекомендации: ${recipe.notes}", style = MaterialTheme.typography.bodyMedium)
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
