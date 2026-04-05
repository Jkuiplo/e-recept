@file:Suppress("DEPRECATION")

package com.google.eRecept.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.eRecept.R
import com.google.eRecept.ui.theme.MainAc
import com.google.eRecept.ui.theme.PrimaryPurple
import com.google.eRecept.ui.theme.SecAc
import com.google.eRecept.ui.theme.SecBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Preview(showBackground = true)
@Composable
fun HomeScreen() {
    val currentDate =
        remember {
            val sdf = SimpleDateFormat("EEEE, d MMMM, yyyy", Locale("ru"))
            val formatted = sdf.format(Date())
            formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }
        }

    val dummyPatients =
        listOf(
            "Қазыбек Нұрым Байболсынұлы",
            "Қазыбек Нұрым Байболсынұлы",
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
            text = "Главная",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        DoctorProfileCard(
            initial = "А",
            fullName = "Молдабекова Дана Ғабиденқызы",
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = currentDate,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ActionCard(
                iconVector = Icons.Default.PersonAdd,
                text = "Добавить\nпациента",
                modifier = Modifier.weight(1f),
            )
            ActionCard(
                iconResId = R.drawable.ic_assignment_add_filled,
                text = "Создать\nрецепт",
                modifier = Modifier.weight(1f),
            )
            ActionCard(
                iconResId = R.drawable.ic_pill_filled,
                text = "Поиск\nпрепаратов",
                modifier = Modifier.weight(1f),
            )
            ActionCard(
                iconVector = Icons.Default.Group,
                text = "Поиск\nпациентов",
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Последние пациенты",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            val cornerRadius = 12.dp

            dummyPatients.forEachIndexed { index, name ->
                val shape =
                    when (index) {
                        0 -> RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
                        dummyPatients.lastIndex -> RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
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

@Composable
fun DoctorProfileCard(
    initial: String,
    fullName: String,
) {
    Card(
        onClick = { /* TODO: Открыть профиль врача */ },
        modifier =
            Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawIntoCanvas { canvas ->
                        val paint =
                            Paint().apply {
                                asFrameworkPaint().apply {
                                    isAntiAlias = true
                                    color = android.graphics.Color.TRANSPARENT
                                    setShadowLayer(
                                        8f,
                                        0f,
                                        6f,
                                        android.graphics.Color.argb(80, 0, 0, 0),
                                    )
                                }
                            }
                        canvas.drawRoundRect(
                            left = 0f,
                            top = 0f,
                            right = size.width,
                            bottom = size.height,
                            radiusX = 16.dp.toPx(),
                            radiusY = 16.dp.toPx(),
                            paint = paint,
                        )
                    }
                }.background(
                    color = Color(0xFFCFC6BC),
                    shape = RoundedCornerShape(100.dp),
                ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SecBg),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }

            Text(
                text = fullName,
                modifier = Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ActionCard(
    text: String,
    modifier: Modifier = Modifier,
    iconVector: ImageVector? = null,
    iconResId: Int? = null,
) {
    Card(
        onClick = { /* TODO: Навигация */ },
        modifier =
            modifier
                .height(100.dp)
                .drawBehind {
                    drawIntoCanvas { canvas ->
                        val paint =
                            Paint().apply {
                                asFrameworkPaint().apply {
                                    isAntiAlias = true
                                    color = android.graphics.Color.TRANSPARENT
                                    setShadowLayer(
                                        8f,
                                        0f,
                                        6f,
                                        android.graphics.Color.argb(80, 0, 0, 0),
                                    )
                                }
                            }
                        canvas.drawRoundRect(
                            left = 0f,
                            top = 0f,
                            right = size.width,
                            bottom = size.height,
                            radiusX = 16.dp.toPx(),
                            radiusY = 16.dp.toPx(),
                            paint = paint,
                        )
                    }
                }.background(
                    color = Color(0xFFCFC6BC),
                    shape = RoundedCornerShape(100.dp),
                ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MainAc),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (iconVector != null) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp),
                )
            } else if (iconResId != null) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun PatientCard(
    ageAndGender: String,
    name: String,
    notes: String,
    shape: Shape,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = SecAc),
        onClick = { /* TODO: Открыть профиль пациента */ },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = ageAndGender,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text =
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.Red)) {
                            append("Примечания: ")
                        }
                        append(notes)
                    },
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
