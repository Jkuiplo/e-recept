package com.google.eRecept.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.eRecept.ui.theme.MainAc
import com.google.eRecept.ui.theme.SecBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onCreateRecipeClick: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

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
            text = "История рецептов",
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Поиск
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Поиск рецепта") },
            trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors =
                OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SecBg,
                    focusedContainerColor = SecBg,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                ),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка создать
        Button(
            onClick = onCreateRecipeClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MainAc, contentColor = MaterialTheme.colorScheme.onBackground),
        ) {
            Icon(Icons.Default.PostAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Создать рецепт", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Рецепты", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Карточка рецепта
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SecBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Рецепт №1234", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text("от 05.04.2026", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Қазыбек Нұрым Байболсынұлы", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
                Text("68 лет · Мужчина", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Омепразол 20 мг, 3 раза в день", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { /* Удалить */ },
                        modifier =
                            Modifier
                                .weight(
                                    1f,
                                ).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onBackground,
                            ),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Удалить")
                    }
                    Button(
                        onClick = { /* Редактировать */ },
                        modifier = Modifier.weight(1.3f),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MainAc,
                                contentColor = MaterialTheme.colorScheme.onBackground,
                            ),
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Редактировать")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
