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
import androidx.compose.material.icons.filled.QrCode
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
import java.util.UUID

data class MedicationEntry(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var dosage: String = "20 мг",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen() {
    var showCreateSheet by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }

    var patientQuery by remember { mutableStateOf("") }
    var medications by remember { mutableStateOf(listOf(MedicationEntry())) }
    var notes by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

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
            "Пенициллин",
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
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Создать рецепт", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showQrDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    // Используем тот же цвет, что и в HomeScreen для консистентности
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "12 апреля 2026",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = patient,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
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

    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            confirmButton = {
                TextButton(onClick = { showQrDialog = false }) { Text("Закрыть") }
            },
            title = { Text("QR-код рецепта") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = null,
                        modifier = Modifier.size(200.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
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
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "Создать рецепт",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
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
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
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
                                        color = MaterialTheme.colorScheme.onSurface,
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

                Button(
                    onClick = { medications = medications + MedicationEntry() },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Добавить препарат", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        showCreateSheet = false
                        resetForm()
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                ) {
                    Text("Создать рецепт", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showCancelDialog = true },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                ) {
                    Text("Отмена", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Отменить создание?") },
            text = { Text("Все введенные данные будут потеряны. Вы уверены?") },
            containerColor = MaterialTheme.colorScheme.surface,
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
                    Text("Продолжить заполнение", color = MaterialTheme.colorScheme.primary)
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
            Text("Препарат ${index + 1}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                        focusManager.moveFocus(FocusDirection.Next)
                    }),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            medTextFieldSize = coordinates.size.toSize()
                        },
                shape = RoundedCornerShape(12.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
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
                                color = MaterialTheme.colorScheme.onSurface,
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color =
                                            if (isSelected) {
                                                Color.Transparent
                                            } else {
                                                MaterialTheme.colorScheme.outline
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                    ).clickable {
                                        onMedicationChange(medication.copy(dosage = dosage))
                                    }.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = dosage,
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}
