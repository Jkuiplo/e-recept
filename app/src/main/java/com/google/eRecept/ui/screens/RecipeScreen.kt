package com.google.eRecept.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties
import com.google.eRecept.ui.theme.MainAc
import com.google.eRecept.ui.theme.SecTx
import java.util.UUID

// Модель для хранения данных препарата
data class MedicationEntry(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var dosage: String = "20 мг",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeScreen() {
    // --- Состояние окон ---
    var showCreateSheet by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    // --- Состояние формы ---
    var patientQuery by remember { mutableStateOf("") }
    var medications by remember { mutableStateOf(listOf(MedicationEntry())) }
    var notes by remember { mutableStateOf("") }

    // skipPartiallyExpanded = true фиксит дерганья модалки при появлении клавиатуры и новых элементов
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

    // Моковые данные
    val mockPatients =
        listOf(
            "Қазыбек Нұрым Байболсынұлы",
            "Қазыбек Ерасыл Арманұлы",
            "Қазыбек Данияр Маратұлы",
            "Азамат Азаматов",
            "Иванов Иван",
        ).filter { it.contains(patientQuery, ignoreCase = true) }

    val mockMeds =
        listOf(
            "Омепразол (Тева)",
            "Омепразол (Акрихин)",
            "Омепразол (Ozon)",
            "Пеницилин",
            "Аспирин",
        )
    val dosages = listOf("20 мг", "35 мг", "40 мг", "50 мг", "75 мг", "100 мг", "120 мг", "200 мг")

    val dummyHistory =
        listOf(
            "Қазыбек Нұрым Байболсынұлы" to "Аспирин, Парацетамол",
            "Иванова Анна" to "Амоксициллин",
            "Смирнов Петр" to "Ибупрофен, Витамин С, Сироп от кашля",
        )

    val resetForm = {
        patientQuery = ""
        medications = listOf(MedicationEntry())
        notes = ""
    }

    // --- Главный экран ---
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Рецепты",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { showCreateSheet = true },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MainAc),
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Создать рецепт", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "История рецептов",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            items(dummyHistory) { (patient, meds) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "12 апреля 2026", style = MaterialTheme.typography.labelMedium, color = SecTx)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = patient, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Препараты: $meds",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    // --- Модальное окно создания рецепта ---
    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            var patientExpanded by remember { mutableStateOf(false) }
            var patientTextFieldSize by remember { mutableStateOf(Size.Zero) }

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "Создать рецепт",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // БЛОК: ПАЦИЕНТ
                Box {
                    OutlinedTextField(
                        value = patientQuery,
                        onValueChange = {
                            patientQuery = it
                            patientExpanded = true
                        },
                        label = { Text("Пациент") },
                        placeholder = { Text("Поиск пациента") },
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
                        // FocusDirection.Next корректно перекидывает на следующее поле
                        keyboardActions =
                            KeyboardActions(onNext = {
                                patientExpanded = false
                                focusManager.moveFocus(FocusDirection.Next)
                            }),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    patientTextFieldSize = coordinates.size.toSize()
                                },
                        shape = RoundedCornerShape(8.dp),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            ),
                        singleLine = true,
                    )

                    DropdownMenu(
                        expanded = patientExpanded && patientQuery.isNotEmpty() && mockPatients.isNotEmpty(),
                        onDismissRequest = { patientExpanded = false },
                        properties = PopupProperties(focusable = false),
                        modifier =
                            Modifier
                                .width(with(density) { patientTextFieldSize.width.toDp() })
                                .heightIn(max = 240.dp)
                                .background(MaterialTheme.colorScheme.surface),
                    ) {
                        mockPatients.forEachIndexed { index, patientName ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = patientName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                },
                                onClick = {
                                    patientQuery = patientName
                                    patientExpanded = false
                                    focusManager.moveFocus(FocusDirection.Next)
                                },
                            )
                            if (index < mockPatients.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // БЛОК: ПРЕПАРАТЫ
                medications.forEachIndexed { index, med ->
                    MedicationItem(
                        index = index,
                        medication = med,
                        mockMeds = mockMeds,
                        dosages = dosages,
                        onMedicationChange = { updatedMed ->
                            medications = medications.map { if (it.id == updatedMed.id) updatedMed else it }
                        },
                        onRemove =
                            if (medications.size > 1) {
                                { medications = medications.filterNot { it.id == med.id } }
                            } else {
                                null
                            },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Кнопка добавления нового препарата
                Button(
                    onClick = { medications = medications + MedicationEntry() },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .drawShadowBackground(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MainAc),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Добавить препарат",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Примечания (последнее поле, поэтому ImeAction.Done)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Примечания") },
                    placeholder = { Text("Введите примечание") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                    shape = RoundedCornerShape(16.dp),
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Финальные кнопки
                Button(
                    onClick = {
                        showCreateSheet = false
                        resetForm()
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .drawShadowBackground(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MainAc),
                ) {
                    Text(
                        "Создать рецепт",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showCancelDialog = true },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .drawShadowBackground(),
                    shape = RoundedCornerShape(24.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onBackground,
                        ),
                ) {
                    Text("Отмена", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                // Дополнительный спейсер снизу, чтобы при скролле элементы не прилипали к краю
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // --- Диалог подтверждения отмены ---
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Отменить создание?") },
            text = { Text("Все введенные данные будут потеряны. Вы уверены?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        showCreateSheet = false
                        resetForm()
                    },
                ) {
                    Text("Да, отменить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Продолжить заполнение")
                }
            },
        )
    }
}

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

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Препарат ${index + 1}", fontWeight = FontWeight.Bold, color = MainAc)
            if (onRemove != null) {
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
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
                label = { Text("Название препарата") },
                placeholder = { Text("Поиск препарата") },
                trailingIcon = {
                    if (medication.name.isNotEmpty()) {
                        IconButton(onClick = {
                            onMedicationChange(medication.copy(name = ""))
                            medExpanded = false
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                        }
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Поиск")
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions =
                    KeyboardActions(onNext = {
                        medExpanded = false
                        focusManager.moveFocus(FocusDirection.Next) // Правильный переход к следующему полю
                    }),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            medTextFieldSize = coordinates.size.toSize()
                        },
                shape = RoundedCornerShape(8.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    ),
                singleLine = true,
            )

            DropdownMenu(
                expanded = medExpanded && medication.name.isNotEmpty() && filteredMeds.isNotEmpty(),
                onDismissRequest = { medExpanded = false },
                properties = PopupProperties(focusable = false),
                modifier =
                    Modifier
                        .width(with(density) { medTextFieldSize.width.toDp() })
                        .heightIn(max = 150.dp)
                        .background(MaterialTheme.colorScheme.surface),
            ) {
                filteredMeds.forEachIndexed { idx, medName ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = medName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        },
                        onClick = {
                            onMedicationChange(medication.copy(name = medName))
                            medExpanded = false
                            focusManager.moveFocus(FocusDirection.Next)
                        },
                    )
                    if (idx < filteredMeds.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // БЛОК: ДОЗИРОВКИ (CHIPS)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val chunkedDosages = dosages.chunked(4)

            chunkedDosages.forEach { rowDosages ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowDosages.forEach { dosage ->
                        val isSelected = medication.dosage == dosage
                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MainAc else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color =
                                            if (isSelected) {
                                                Color.Transparent
                                            } else {
                                                MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.5f,
                                                )
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                    ).clickable {
                                        // УБРАЛ clearFocus(), чтобы клавиатура не пропадала при выборе граммовки!
                                        onMedicationChange(medication.copy(dosage = dosage))
                                    }.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = dosage,
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    ),
                                // Если чипс выбран (он синий), текст будет БЕЛЫМ. Иначе обычным.
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
            }
        }
    }
}

// Экстеншен для тени: убрал хардкод .background(), чтобы он не перебивал цвета ButtonDefaults
fun Modifier.drawShadowBackground(): Modifier =
    this
        .drawBehind {
            drawIntoCanvas { canvas ->
                val paint =
                    Paint().apply {
                        asFrameworkPaint().apply {
                            isAntiAlias = true
                            color = android.graphics.Color.TRANSPARENT
                            setShadowLayer(8f, 0f, 6f, android.graphics.Color.argb(40, 0, 0, 0)) // Сделал тень чуть мягче (40 вместо 80)
                        }
                    }
                canvas.drawRoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height,
                    radiusX = 24.dp.toPx(),
                    radiusY = 24.dp.toPx(),
                    paint = paint,
                )
            }
        }
