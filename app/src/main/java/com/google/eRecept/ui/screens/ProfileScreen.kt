package com.google.eRecept.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    // --- Состояния ---
    var showLogoutDialog by remember { mutableStateOf(false) }

    // В реальном приложении это состояние должно приходить из ViewModel/DataStore
    // 0 = Системная, 1 = Светлая, 2 = Темная
    var selectedThemeIndex by remember { mutableStateOf(0) }
    val themeOptions = listOf("Авто", "Светлая", "Темная")

    // Моковые данные врача (нельзя редактировать напрямую тут)
    val doctorName = "Иванов Иван Иванович"
    val doctorSpecialization = "Врач-терапевт, Кардиолог"

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // ==========================================
        // БЛОК: АВАТАРКА
        // ==========================================
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.padding(bottom = 24.dp),
        ) {
            // Сама аватарка
            Box(
                modifier =
                    Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                // Заглушка, если нет фото. Позже тут будет AsyncImage из Coil
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Аватар",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            // Кнопка редактирования аватарки
            FilledIconButton(
                onClick = { /* TODO: Вызов лончера для выбора картинки из галереи */ },
                modifier =
                    Modifier
                        .size(36.dp)
                        .offset(x = 8.dp, y = 8.dp),
                // Слегка выносим за пределы круга
                shape = CircleShape,
                colors =
                    IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Изменить фото",
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        // ==========================================
        // БЛОК: ИНФО (Только для чтения)
        // ==========================================
        Text(
            text = doctorName,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = doctorSpecialization,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(48.dp))

        // ==========================================
        // БЛОК: НАСТРОЙКИ
        // ==========================================
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp, start = 8.dp),
            )

            // Смена темы (Сегментированные кнопки)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Оформление", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        themeOptions.forEachIndexed { index, title ->
                            SegmentedButton(
                                selected = selectedThemeIndex == index,
                                onClick = {
                                    selectedThemeIndex = index
                                    // TODO: Отправить Intent/State вверх во ViewModel для реальной смены темы
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = themeOptions.size),
                                colors =
                                    SegmentedButtonDefaults.colors(
                                        activeContainerColor = MaterialTheme.colorScheme.primary,
                                        activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                        inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                                    ),
                            ) {
                                Text(title)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Смена пароля
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { /* TODO: Переход на экран смены пароля */ },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Сменить пароль", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Выталкивает кнопку выхода вниз
        Spacer(modifier = Modifier.height(32.dp))

        // ==========================================
        // БЛОК: ВЫХОД
        // ==========================================
        Button(
            onClick = { showLogoutDialog = true },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Выйти из аккаунта", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // --- Диалог подтверждения выхода ---
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
            title = { Text("Выход из аккаунта") },
            text = { Text("Вы уверены, что хотите выйти? Вам потребуется заново ввести логин и пароль.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        // TODO: Логика очистки токенов и переход на экран авторизации
                    },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                ) {
                    Text("Выйти")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Отмена", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
