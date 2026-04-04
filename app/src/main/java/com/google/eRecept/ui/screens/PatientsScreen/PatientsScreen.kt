package com.google.eRecept.ui.screens.PatientsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.eRecept.ui.screens.PatientCard
import com.google.eRecept.ui.theme.MainAc
import com.google.eRecept.ui.theme.SecBg

@Composable
fun PatientsScreen(
    onAddPatientClick: () -> Unit, // Коллбэк для перехода на экран добавления
) {
    var searchQuery by remember { mutableStateOf("") }

    val dummyPatients =
        listOf(
            "Қазыбек Нұрым Байболсынұлы",
            "Иванов Иван Иванович",
            "Смирнова Анна Сергеевна",
            "Қазыбек Нұрым Байболсынұлы",
            "Қазыбек Нұрым Байболсынұлы",
        )

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
            text = "Пациенты",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Поле поиска (овальное, без лишних линий)
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Поиск пациента") },
            trailingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Поиск")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = SecBg,
                    unfocusedContainerColor = SecBg,
                    focusedIndicatorColor = Color.Transparent, // Убираем нижнюю линию
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка "Добавить пациента"
        Button(
            onClick = onAddPatientClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MainAc,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Добавить пациента",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Мои пациенты",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Список пациентов (слитный, как на Главной)
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            val cornerRadius = 16.dp

            dummyPatients.forEachIndexed { index, name ->
                val shape =
                    when {
                        dummyPatients.size == 1 -> RoundedCornerShape(cornerRadius)
                        index == 0 -> RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
                        index == dummyPatients.lastIndex -> RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
                        else -> RoundedCornerShape(0.dp)
                    }

                PatientCard(
                    ageAndGender = "68 лет · Мужчина",
                    name = name,
                    notes = "Аллергия (пеницилин), Сахарный диабет II типа, Ишемическая болезнь сердца",
                    shape = shape,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
