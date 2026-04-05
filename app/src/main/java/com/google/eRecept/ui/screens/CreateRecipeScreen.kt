package com.google.eRecept.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusDirection
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
import com.google.eRecept.ui.theme.SecBg

@OptIn(ExperimentalLayoutApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun CreateRecipeScreen() {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

    // Стейты Пациента
    var patientExpanded by remember { mutableStateOf(false) }
    var patientQuery by remember { mutableStateOf("") }
    var patientTextFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    // Стейты Препарата
    var medExpanded by remember { mutableStateOf(false) }
    var medQuery by remember { mutableStateOf("") }
    var medTextFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    var selectedDosage by remember { mutableStateOf("20 мг") }
    var notes by remember { mutableStateOf("") }

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
        ).filter { it.contains(medQuery, ignoreCase = true) }

    val dosages = listOf("20 мг", "35 мг", "40 мг", "50 мг", "75 мг", "100 мг", "120 мг", "200 мг")

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Создать рецепт",
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // БЛОК: ПАЦИЕНТ
        // ==========================================
        Box {
            OutlinedTextField(
                value = patientQuery,
                onValueChange = {
                    patientQuery = it
                    patientExpanded = true
                },
                label = { Text("Пациент") },
                placeholder = { Text("Поиск пациента") },
                // КРЕСТИК ИЛИ ЛУПА В ЗАВИСИМОСТИ ОТ ВВОДА
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
                        focusManager.moveFocus(FocusDirection.Down)
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
                        .background(SecBg),
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
                            focusManager.moveFocus(FocusDirection.Down)
                        },
                    )
                    if (index < mockPatients.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // БЛОК: ПРЕПАРАТ 1
        // ==========================================
        Box {
            OutlinedTextField(
                value = medQuery,
                onValueChange = {
                    medQuery = it
                    medExpanded = true
                },
                label = { Text("Препарат 1") },
                placeholder = { Text("Поиск препарата") },
                trailingIcon = {
                    if (medQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            medQuery = ""
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
                        focusManager.moveFocus(FocusDirection.Down)
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
                expanded = medExpanded && medQuery.isNotEmpty() && mockMeds.isNotEmpty(),
                onDismissRequest = { medExpanded = false },
                properties = PopupProperties(focusable = false),
                modifier =
                    Modifier
                        .width(with(density) { medTextFieldSize.width.toDp() })
                        .heightIn(max = 150.dp)
                        .background(SecBg),
            ) {
                mockMeds.forEachIndexed { index, medName ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = medName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        },
                        onClick = {
                            medQuery = medName
                            medExpanded = false
                            focusManager.moveFocus(FocusDirection.Down)
                        },
                    )
                    if (index < mockMeds.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // БЛОК: ДОЗИРОВКИ (CHIPS)
        // ==========================================
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
                        val isSelected = selectedDosage == dosage
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
                                        selectedDosage = dosage
                                        focusManager.clearFocus()
                                    }.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = dosage,
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    ),
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Кнопка "Добавить препарат"
        Button(
            onClick = { /* TODO */ },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .drawBehind {
                        drawIntoCanvas { canvas ->
                            val paint =
                                Paint().apply {
                                    asFrameworkPaint().apply {
                                        isAntiAlias = true
                                        color = android.graphics.Color.TRANSPARENT
                                        setShadowLayer(8f, 0f, 6f, android.graphics.Color.argb(80, 0, 0, 0))
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
                    }.background(color = Color(0xFFCFC6BC), shape = RoundedCornerShape(100.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MainAc, contentColor = MaterialTheme.colorScheme.onBackground),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Добавить препарат", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Примечания
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Примечания") },
            placeholder = { Text("Введите примечание") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(16.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Финальные кнопки
        Button(
            onClick = { /* TODO */ },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .drawBehind {
                        drawIntoCanvas { canvas ->
                            val paint =
                                Paint().apply {
                                    asFrameworkPaint().apply {
                                        isAntiAlias = true
                                        color = android.graphics.Color.TRANSPARENT
                                        setShadowLayer(8f, 0f, 6f, android.graphics.Color.argb(80, 0, 0, 0))
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
                    }.background(color = Color(0xFFCFC6BC), shape = RoundedCornerShape(100.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MainAc, contentColor = MaterialTheme.colorScheme.onBackground),
        ) {
            Text("Создать рецепт", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* TODO: Назад */ },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .drawBehind {
                        drawIntoCanvas { canvas ->
                            val paint =
                                Paint().apply {
                                    asFrameworkPaint().apply {
                                        isAntiAlias = true
                                        color = android.graphics.Color.TRANSPARENT
                                        setShadowLayer(8f, 0f, 6f, android.graphics.Color.argb(80, 0, 0, 0))
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
                    }.background(color = Color(0xFFCFC6BC), shape = RoundedCornerShape(100.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SecBg, contentColor = MaterialTheme.colorScheme.onBackground),
        ) {
            Text("Отмена", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
        val isKeyboardOpen = WindowInsets.isImeVisible

        val bottomSpacerHeight by animateDpAsState(
            targetValue = if (isKeyboardOpen) 200.dp else 32.dp,
            label = "keyboard_spacer_animation",
        )

        Spacer(modifier = Modifier.height(bottomSpacerHeight).imePadding())
    }
}
