package com.google.eRecept.ui.screens

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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties
import java.util.UUID

data class MedicationEntry(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var dosage: String = "",
)

data class RecipeHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val patientIin: String,
    val date: String,
    val medications: List<String>,
    val isActive: Boolean,
)

// Mock-база данных: Препарат -> Доступные дозировки
val mockMedsDb =
    mapOf(
        "Амоксициллин" to listOf("250 мг", "500 мг", "1000 мг"),
        "Аспирин" to listOf("100 мг", "500 мг"),
        "Ибупрофен" to listOf("200 мг", "400 мг", "Сироп 100 мг/5 мл"),
        "Омепразол" to listOf("10 мг", "20 мг", "40 мг"),
        "Парацетамол" to listOf("500 мг", "Сироп 120 мг/5 мл"),
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen() {
    var showCreateSheet by remember { mutableStateOf(false) }
    var selectedRecipe by remember { mutableStateOf<RecipeHistoryItem?>(null) }
    val focusManager = LocalFocusManager.current

    // Состояние формы (Черновик)
    var patientIin by remember { mutableStateOf("") }
    var medications by remember { mutableStateOf(listOf(MedicationEntry())) }
    var notes by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val dummyHistory =
        remember {
            listOf(
                RecipeHistoryItem(
                    patientIin = "010101500123",
                    date = "13 апреля 2024",
                    medications = listOf("Омепразол", "Амоксициллин"),
                    isActive = true,
                ),
                RecipeHistoryItem(
                    patientIin = "950202400456",
                    date = "05 марта 2024",
                    medications = listOf("Амоксициллин"),
                    isActive = false,
                ),
            )
        }

    val resetForm = {
        patientIin = ""
        medications = listOf(MedicationEntry())
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
                items(dummyHistory, key = { it.id }) { recipe ->
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
            // Просто скрываем шторку, сохраняя данные в переменных (Черновик)
            onDismissRequest = { showCreateSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
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
                        Icon(Icons.Default.Close, contentDescription = "Свернуть черновик")
                    }
                    Text(
                        text = "Новый рецепт",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )

                    // Кнопка принудительной очистки формы
                    IconButton(onClick = resetForm) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Очистить форму", tint = MaterialTheme.colorScheme.error)
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
                            }
                        },
                        label = { Text("ИИН пациента") },
                        placeholder = { Text("12 цифр") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        // Строго Numpad и кнопка "Далее"
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword, // В M3 NumberPassword часто дает чистый Numpad
                                imeAction = ImeAction.Next,
                            ),
                        keyboardActions =
                            KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Next) },
                            ),
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Назначения",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    medications.forEachIndexed { index, med ->
                        SmartMedicationRow(
                            index = index,
                            medication = med,
                            onMedicationChange = { updatedMed ->
                                medications = medications.map { if (it.id == updatedMed.id) updatedMed else it }
                            },
                            onRemove =
                                if (medications.size > 1) {
                                    { medications = medications.filterNot { it.id == med.id } }
                                } else {
                                    null
                                },
                            focusManager = focusManager,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    TextButton(
                        onClick = { medications = medications + MedicationEntry() },
                        modifier = Modifier.padding(vertical = 8.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Добавить препарат")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Рекомендации и примечания") },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions =
                            KeyboardActions(
                                onDone = { focusManager.clearFocus() },
                            ),
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                }

                Button(
                    onClick = {
                        // TODO: Логика отправки на сервер
                        showCreateSheet = false
                        resetForm() // Очищаем только после успешной выписки
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = patientIin.length == 12 && medications.first().name.isNotBlank(),
                ) {
                    Text("Выписать рецепт", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun SmartMedicationRow(
    index: Int,
    medication: MedicationEntry,
    onMedicationChange: (MedicationEntry) -> Unit,
    onRemove: (() -> Unit)?,
    focusManager: androidx.compose.ui.focus.FocusManager,
) {
    val density = LocalDensity.current

    // Состояния для Dropdown меню
    var nameExpanded by remember { mutableStateOf(false) }
    var nameFieldSize by remember { mutableStateOf(Size.Zero) }

    var dosageExpanded by remember { mutableStateOf(false) }
    var dosageFieldSize by remember { mutableStateOf(Size.Zero) }

    // Фильтрация лекарств
    val suggestedMeds =
        mockMedsDb.keys.filter {
            it.contains(medication.name, ignoreCase = true) && medication.name.isNotBlank()
        }

    // Подбор дозировок на основе точного совпадения или хотя бы частично введенного названия
    val matchingMedKey = mockMedsDb.keys.firstOrNull { it.equals(medication.name, ignoreCase = true) }
    val suggestedDosages = mockMedsDb[matchingMedKey] ?: emptyList()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Поле Препарата
        Box(modifier = Modifier.weight(0.55f)) {
            OutlinedTextField(
                value = medication.name,
                onValueChange = {
                    onMedicationChange(medication.copy(name = it, dosage = "")) // Сбрасываем дозу при смене препарата
                    nameExpanded = true
                },
                label = { Text("Препарат") },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { nameFieldSize = it.size.toSize() },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
            )

            DropdownMenu(
                expanded = nameExpanded && suggestedMeds.isNotEmpty(),
                onDismissRequest = { nameExpanded = false },
                properties = PopupProperties(focusable = false),
                modifier = Modifier.width(with(density) { nameFieldSize.width.toDp() }),
            ) {
                suggestedMeds.forEach { medName ->
                    DropdownMenuItem(
                        text = { Text(medName) },
                        onClick = {
                            onMedicationChange(medication.copy(name = medName))
                            nameExpanded = false
                            focusManager.moveFocus(FocusDirection.Next) // Прыгаем на дозировку
                        },
                    )
                }
            }
        }

        // Поле Дозировки
        Box(modifier = Modifier.weight(0.45f)) {
            OutlinedTextField(
                value = medication.dosage,
                onValueChange = {
                    onMedicationChange(medication.copy(dosage = it))
                    dosageExpanded = true
                },
                label = { Text("Дозировка") },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { dosageFieldSize = it.size.toSize() }
                        .onFocusChanged {
                            // Показываем подсказки дозировки, когда поле получает фокус
                            if (it.isFocused && suggestedDosages.isNotEmpty()) {
                                dosageExpanded = true
                            }
                        },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
            )

            DropdownMenu(
                expanded = dosageExpanded && suggestedDosages.isNotEmpty(),
                onDismissRequest = { dosageExpanded = false },
                properties = PopupProperties(focusable = false),
                modifier = Modifier.width(with(density) { dosageFieldSize.width.toDp() }),
            ) {
                suggestedDosages.forEach { dosage ->
                    DropdownMenuItem(
                        text = { Text(dosage) },
                        onClick = {
                            onMedicationChange(medication.copy(dosage = dosage))
                            dosageExpanded = false
                            focusManager.moveFocus(FocusDirection.Next)
                        },
                    )
                }
            }
        }

        if (onRemove != null) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveCircleOutline,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
fun RecipeHistoryCard(
    recipe: RecipeHistoryItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ИИН: ${recipe.patientIin}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${recipe.date} • ${recipe.medications.size} препарата(ов)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            val badgeColor = if (recipe.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            val badgeContainer = if (recipe.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant

            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = if (recipe.isActive) "Активен" else "Истек",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = badgeColor,
                )
            }
        }
    }
}

@Composable
fun RecipeDetailsDialog(
    recipe: RecipeHistoryItem,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        },
        title = { Text("Детали рецепта") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.QrCode2,
                        contentDescription = "QR Code",
                        modifier = Modifier.size(140.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                Text("ИИН Пациента", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(recipe.patientIin, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Дата выписки", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(recipe.date, style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("Назначения", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                recipe.medications.forEach { med ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp),
                    ) {
                        Icon(
                            Icons.Default.Circle,
                            contentDescription = null,
                            modifier = Modifier.size(6.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(med, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
    )
}
