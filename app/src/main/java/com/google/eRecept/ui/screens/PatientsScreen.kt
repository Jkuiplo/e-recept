package com.google.eRecept.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.eRecept.ui.theme.MainAc
import com.google.eRecept.ui.theme.SecBg
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(
    focusSearchOnStart: Boolean = false,
    onAddPatientClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusSearchOnStart) {
        if (focusSearchOnStart) {
            focusRequester.requestFocus()
        }
    }

    val dummyPatients = listOf(
        "Қазыбек Нұрым Байболсынұлы",
        "Иванов Иван Иванович",
        "Смирнова Анна Сергеевна",
        "Қазыбек Нұрым Байболсынұлы",
        "Қазыбек Нұрым Байболсынұлы",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Пациенты", 
            style = MaterialTheme.typography.headlineLarge, 
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Поиск пациента") },
            trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .drawShadowCustom(radius = 24.dp)
                .background(Color(0xFFCFC6BC), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SecBg, 
                unfocusedContainerColor = SecBg,
                focusedIndicatorColor = Color.Transparent, 
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAddPatientClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .drawShadowCustom(radius = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MainAc, 
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Добавить пациента", 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Мои пациенты", 
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientScreenContent(onBackClick: () -> Unit) {
    var iin by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isMale by remember { mutableStateOf(true) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val focusManager = LocalFocusManager.current
    var isError by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("ddMMyyyy", Locale("ru"))
                        birthDate = sdf.format(Date(millis))
                        isError = false
                    }
                    showDatePicker = false
                }) {
                    Text("Выбрать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Добавить пациента",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        OutlinedTextField(
            value = iin,
            onValueChange = { iin = it },
            label = { Text("ИИН") },
            placeholder = { Text("Введите ИИН пациента") },
            supportingText = { Text("Например: 012 345 678 910") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            trailingIcon = {
                if (iin.isNotEmpty()) {
                    IconButton(onClick = { iin = "" }) { Icon(Icons.Default.Clear, "") }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Фамилия") },
            placeholder = { Text("Введите фамилию пациента") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("Имя") },
            placeholder = { Text("Введите имя пациента") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = middleName,
            onValueChange = { middleName = it },
            label = { Text("Отчество") },
            placeholder = { Text("Введите отчество пациента") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = birthDate,
            onValueChange = { newValue ->
                val digitsOnly = newValue.filter { it.isDigit() }.take(8)
                birthDate = digitsOnly

                isError =
                    if (digitsOnly.length == 8) {
                        !isValidDate(digitsOnly)
                    } else {
                        false
                    }
            },
            isError = isError,
            label = { Text("Дата рождения") },
            placeholder = { Text("ДД.ММ.ГГГГ") },
            supportingText = {
                if (isError) {
                    Text("Неверная или несуществующая дата", color = MaterialTheme.colorScheme.error)
                }
            },
            visualTransformation = DateTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            trailingIcon = {
                IconButton(onClick = {
                    focusManager.clearFocus()
                    showDatePicker = true
                }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Календарь")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Пол пациента",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
        ) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(if (isMale) SecBg else Color.Transparent)
                        .clickable {
                            focusManager.clearFocus()
                            isMale = true
                        },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isMale) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text("Мужчина", fontWeight = if (isMale) FontWeight.Bold else FontWeight.Normal)
                }
            }

            Box(modifier = Modifier.width(1.dp).fillMaxSize().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(if (!isMale) SecBg else Color.Transparent)
                        .clickable {
                            focusManager.clearFocus()
                            isMale = false
                        },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isMale) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text("Женщина", fontWeight = if (!isMale) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Примечания") },
            placeholder = { Text("Введите примечание") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(8.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { /* TODO: Сохранить */ },
            modifier = Modifier.fillMaxWidth().height(56.dp).drawShadowCustom(radius = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MainAc, contentColor = MaterialTheme.colorScheme.onBackground),
        ) {
            Text("Добавить", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth().height(56.dp).drawShadowCustom(radius = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SecBg, contentColor = MaterialTheme.colorScheme.onBackground),
        ) {
            Text("Отмена", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}

fun Modifier.drawShadowCustom(
    radius: androidx.compose.ui.unit.Dp,
    alpha: Int = 80,
    shadowRadius: Float = 8f,
    offsetY: Float = 6f
) = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            asFrameworkPaint().apply {
                isAntiAlias = true
                color = android.graphics.Color.TRANSPARENT
                setShadowLayer(
                    shadowRadius,
                    0f,
                    offsetY,
                    android.graphics.Color.argb(alpha, 0, 0, 0),
                )
            }
        }
        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = radius.toPx(),
            radiusY = radius.toPx(),
            paint = paint,
        )
    }
}

fun isValidDate(input: String): Boolean {
    if (input.length != 8) return false
    return try {
        val formatter =
            DateTimeFormatter
                .ofPattern("ddMMuuuu")
                .withResolverStyle(ResolverStyle.STRICT)
        LocalDate.parse(input, formatter)
        true
    } catch (_: Exception) {
        false
    }
}

class DateTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 8) text.text.substring(0..7) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1 || i == 3) out += "." // Ставим точки после дня и месяца
        }

        val offsetTranslator =
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset <= 1) return offset
                    if (offset <= 3) return offset + 1
                    if (offset <= 8) return offset + 2
                    return 10
                }

                override fun transformedToOriginal(offset: Int): Int {
                    if (offset <= 2) return offset
                    if (offset <= 5) return offset - 1
                    if (offset <= 10) return offset - 2
                    return 8
                }
            }
        return TransformedText(AnnotatedString(out), offsetTranslator)
    }
}
