package com.google.eRecept.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
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
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
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

    val draftPatientIin by viewModel.draftPatientIin.collectAsStateWithLifecycle()
    val draftMedications by viewModel.draftMedications.collectAsStateWithLifecycle()
    val draftNotes by viewModel.draftNotes.collectAsStateWithLifecycle()
    val draftExpireDays by viewModel.draftExpireDays.collectAsStateWithLifecycle()

    val patientResult by homeViewModel.searchPatientResult.collectAsStateWithLifecycle()
    val isSearchingPatient by homeViewModel.isSearching.collectAsStateWithLifecycle()

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
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
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
                        .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
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
                                            val newList = draftMedications.toMutableList()
                                            newList.removeAt(index)
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
                        Text(
                            text = "Настройки рецепта",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Срок действия рецепта (дней)", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        CustomSegmentedControl(
                            options = listOf("10", "15", "30", "60"),
                            selectedOption = draftExpireDays.toString(),
                            onOptionSelected = { viewModel.updateDraftExpireDays(it.toIntOrNull() ?: 30) },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = draftNotes,
                            onValueChange = { viewModel.updateDraftNotes(it) },
                            label = { Text("Общие рекомендации (опционально)") },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                            shape = RoundedCornerShape(12.dp),
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                        ) {
                            Column {
                                Text(
                                    "Итоговый рецепт:",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                draftMedications.forEach { med ->
                                    if (med.name.isNotBlank()) {
                                        Text("• ${med.name}", fontWeight = FontWeight.Bold)
                                        Text("  ${med.summary}", style = MaterialTheme.typography.bodySmall)
                                        if (med.note.isNotBlank()) {
                                            Text(
                                                "  Прим: ${med.note}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f),
                                )
                                Text(
                                    "Действителен: $draftExpireDays дней",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }

                    Button(
                        onClick = { viewModel.createRecipe(patientResult?.full_name ?: "Неизвестно") },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                                .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled =
                            draftPatientIin.length == 12 &&
                                patientResult != null &&
                                draftMedications.isNotEmpty() &&
                                draftMedications.all { it.id.isNotBlank() },
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
    val expireStr = sdf.format(Date(recipe.expire_date))
    val recipeNum = recipe.id.takeLast(4).uppercase()

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
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

            Spacer(modifier = Modifier.width(12.dp))

            val badgeContainerColor = if (recipe.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
            val badgeContentColor = if (recipe.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeContainerColor)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = if (recipe.isActive) "Активен" else "Истек",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = badgeContentColor,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "до $expireStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeContentColor.copy(alpha = 0.8f),
                )
            }
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
    val durationUnits = listOf("дн.", "нед", "мес")

    // Локальная функция для обработки ввода цифр (защита от <= 0)
    fun safeNumberInput(input: String): String {
        val filtered = input.filter { it.isDigit() || it == '.' }
        if (filtered == "0" || filtered.startsWith("-")) return "1"
        return filtered
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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

            Spacer(modifier = Modifier.height(12.dp))

            // 1. НАЗВАНИЕ
            Box {
                val isMedicationNotSelected = medication.name.isNotBlank() && medication.id.isBlank()

                OutlinedTextField(
                    value = medication.name,
                    onValueChange = {
                        // ВАЖНО: Если пользователь меняет текст руками, сбрасываем ID
                        onMedicationChange(medication.copy(name = it, id = ""))
                        viewModel.searchMedications(it)
                        nameExpanded = true
                    },
                    label = { Text("Название") },
                    isError = isMedicationNotSelected,
                    supportingText = {
                        if (isMedicationNotSelected) {
                            Text("Обязательно выберите препарат из списка")
                        }
                    },
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
                                // Сохраняем ID при клике
                                onMedicationChange(
                                    medication.copy(
                                        id = suggestion.id,
                                        name = suggestion.name,
                                    ),
                                )
                                nameExpanded = false
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. ДОЗИРОВКА (Компактный дизайн)
            Text("Дозировка", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = medication.dosageValue,
                    onValueChange = { onMedicationChange(medication.copy(dosageValue = safeNumberInput(it))) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                    leadingIcon = {
                        IconButton(onClick = {
                            val current = medication.dosageValue.toDoubleOrNull() ?: 1.0
                            if (current >
                                1.0
                            ) {
                                onMedicationChange(medication.copy(dosageValue = (current - 1).toString().removeSuffix(".0")))
                            }
                        }) { Icon(Icons.Default.Remove, "Меньше", tint = MaterialTheme.colorScheme.primary) }
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            val current = medication.dosageValue.toDoubleOrNull() ?: 0.0
                            onMedicationChange(medication.copy(dosageValue = (current + 1).toString().removeSuffix(".0")))
                        }) { Icon(Icons.Default.Add, "Больше", tint = MaterialTheme.colorScheme.primary) }
                    },
                )

                CustomSegmentedControl(
                    options = dosageUnits,
                    selectedOption = medication.dosageUnit,
                    onOptionSelected = { onMedicationChange(medication.copy(dosageUnit = it)) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. КРАТНОСТЬ
            Text("Кратность приёма", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            CustomSegmentedControl(
                options = frequencies,
                selectedOption = medication.frequency,
                onOptionSelected = { onMedicationChange(medication.copy(frequency = it)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. ДЛИТЕЛЬНОСТЬ (Компактный дизайн)
            Text("Длительность", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = medication.durationValue,
                    onValueChange = { onMedicationChange(medication.copy(durationValue = safeNumberInput(it))) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                    leadingIcon = {
                        IconButton(onClick = {
                            val current = medication.durationValue.toDoubleOrNull() ?: 1.0
                            if (current >
                                1.0
                            ) {
                                onMedicationChange(medication.copy(durationValue = (current - 1).toString().removeSuffix(".0")))
                            }
                        }) { Icon(Icons.Default.Remove, "Меньше", tint = MaterialTheme.colorScheme.primary) }
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            val current = medication.durationValue.toDoubleOrNull() ?: 0.0
                            onMedicationChange(medication.copy(durationValue = (current + 1).toString().removeSuffix(".0")))
                        }) { Icon(Icons.Default.Add, "Больше", tint = MaterialTheme.colorScheme.primary) }
                    },
                )
                CustomSegmentedControl(
                    options = durationUnits,
                    selectedOption = medication.durationUnit,
                    onOptionSelected = { onMedicationChange(medication.copy(durationUnit = it)) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. УКАЗАНИЯ
            OutlinedTextField(
                value = medication.note,
                onValueChange = { onMedicationChange(medication.copy(note = it)) },
                label = { Text("Особые указания (напр. после еды)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )
        }
    }
}

@Composable
fun RecipeDetailsDialog(
    recipe: Recipe,
    onDismiss: () -> Unit,
    viewModel: RecipeViewModel,
) {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    val dateStr = sdf.format(Date(recipe.date))
    val expireStr = sdf.format(Date(recipe.expire_date))

    val qrUrl = "https://e-recepta.vercel.app/recipes/${recipe.id}/qr"

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } },
        title = { Text("Детали рецепта", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    SubcomposeAsyncImage(
                        model = qrUrl,
                        contentDescription = "QR Code",
                        modifier = Modifier.size(200.dp),
                        contentScale = ContentScale.Fit,
                        loading = {
                            CircularProgressIndicator(modifier = Modifier.padding(64.dp))
                        },
                        error = {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Ошибка загрузки",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp),
                            )
                        },
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                val badgeColor = if (recipe.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                val textColor = if (recipe.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier =
                            Modifier
                                .clip(
                                    RoundedCornerShape(8.dp),
                                ).background(badgeColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            if (recipe.isActive) "АКТИВЕН" else "ИСТЕК",
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Выписан: $dateStr", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "Действителен до: $expireStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Пациент: ${recipe.patient_name}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("ИИН: ${recipe.patient_iin}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                recipe.medications.forEach { med ->
                    Text("• ${med.name}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                    Text("  ${med.summary}", style = MaterialTheme.typography.bodySmall)
                    if (med.note.isNotBlank()) {
                        Text(
                            "  Прим: ${med.note}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                if (recipe.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Общие рекомендации: ${recipe.notes}", style = MaterialTheme.typography.bodyMedium)
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

@Composable
fun CustomSegmentedControl(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .height(IntrinsicSize.Min)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selectedOption
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .clickable { onOptionSelected(option) }
                        .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
            if (index < options.size - 1) {
                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}
