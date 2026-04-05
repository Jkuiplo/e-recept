package com.google.eRecept.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.eRecept.ui.theme.MainAc
import com.google.eRecept.ui.theme.SecBg

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalMaterial3Api::class) // Нужен для ExposedDropdownMenuBox
@Composable
fun CreateRecipeScreen() {
    val focusManager = LocalFocusManager.current

    // --- Стейты Пациента ---
    var patientExpanded by remember { mutableStateOf(false) }
    var patientQuery by remember { mutableStateOf("") }

    // --- Стейты Препарата ---
    var medExpanded by remember { mutableStateOf(false) }
    var medQuery by remember { mutableStateOf("") }

    // --- Остальные стейты ---
    var selectedDosage by remember { mutableStateOf("20 мг") }
    var notes by remember { mutableStateOf("") }

    // --- Mock Данные (с имитацией нескольких результатов поиска) ---
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

    // UI
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
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
        // БЛОК: ПАЦИЕНТ (Нативный Overlay Поиск)
        // ==========================================
        ExposedDropdownMenuBox(
            expanded = patientExpanded,
            onExpandedChange = { patientExpanded = !patientExpanded },
        ) {
            OutlinedTextField(
                value = patientQuery,
                onValueChange = {
                    patientQuery = it
                    patientExpanded = true // Открываем меню при вводе
                },
                label = { Text("Пациент") },
                placeholder = { Text("Поиск пациента") },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
                // modifier.menuAnchor() - магия, которая делает ширину выпадающего списка равной полю ввода!
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    ),
                singleLine = true,
            )

            // Выпадающий список (Overlay)
            if (patientQuery.isNotEmpty() && mockPatients.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = patientExpanded,
                    onDismissRequest = { patientExpanded = false },
                    modifier = Modifier.background(SecBg), // Твой бежевый цвет из макета
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
                                focusManager.clearFocus() // Убираем клавиатуру после выбора
                            },
                        )
                        // Тонкая линия-разделитель (кроме последнего элемента)
                        if (index < mockPatients.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // БЛОК: ПРЕПАРАТ 1 (Нативный Overlay Поиск)
        // ==========================================
        ExposedDropdownMenuBox(
            expanded = medExpanded,
            onExpandedChange = { medExpanded = !medExpanded },
        ) {
            OutlinedTextField(
                value = medQuery,
                onValueChange = {
                    medQuery = it
                    medExpanded = true
                },
                label = { Text("Препарат 1") },
                placeholder = { Text("Поиск препарата") },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    ),
                singleLine = true,
            )

            if (medQuery.isNotEmpty() && mockMeds.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = medExpanded,
                    onDismissRequest = { medExpanded = false },
                    modifier = Modifier.background(SecBg),
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
                                focusManager.clearFocus()
                            },
                        )
                        if (index < mockMeds.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        }
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
                                    ).clickable { selectedDosage = dosage }
                                    .padding(vertical = 10.dp),
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

        Spacer(modifier = Modifier.height(32.dp))
    }
}
