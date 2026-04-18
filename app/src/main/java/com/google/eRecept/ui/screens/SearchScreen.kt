package com.google.eRecept.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.eRecept.data.Medication
import com.google.eRecept.data.Patient
import com.google.eRecept.data.Recipe
import com.google.eRecept.ui.viewmodels.SearchViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel) {
    val focusManager = LocalFocusManager.current
    val tabs = listOf("Пациенты", "Препараты", "История")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by rememberSaveable { mutableStateOf("") }

    val patientResults by viewModel.patientResults.collectAsStateWithLifecycle()
    val medicationResults by viewModel.medicationResults.collectAsStateWithLifecycle()
    val allRecipes by viewModel.allRecipes.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var selectedMedication by remember { mutableStateOf<Medication?>(null) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }

    LaunchedEffect(searchQuery, pagerState.currentPage) {
        viewModel.search(searchQuery, pagerState.currentPage)
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Поиск",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text =
                            when (pagerState.currentPage) {
                                0 -> "ФИО или ИИН"
                                1 -> "Название препарата"
                                else -> "Поиск в истории"
                            },
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                shape = CircleShape,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                divider = {},
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                val diff = kotlin.math.abs(pagerState.currentPage - index)
                                if (diff > 1) {
                                    pagerState.scrollToPage(index)
                                } else {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        },
                        text = { Text(title) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
            ) { page ->
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when (page) {
                        0 -> {
                            if (patientResults.isEmpty() && searchQuery.isNotEmpty() && !isSearching) {
                                Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                                    SearchEmptyState("Пациенты не найдены", Icons.Default.PersonOff)
                                }
                            } else if (patientResults.isEmpty() && searchQuery.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                                    SearchEmptyState("Введите ИИН или ФИО для поиска пациента", Icons.Default.Search)
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(bottom = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    items(patientResults, key = { it.iin }) { patient ->
                                        PatientListItem(patient = patient, onClick = { selectedPatient = patient })
                                    }
                                }
                            }
                        }

                        1 -> {
                            if (isSearching && medicationResults.isEmpty()) {
                                MedicationSkeletonList()
                            } else if (medicationResults.isEmpty() && searchQuery.isNotEmpty() && !isSearching) {
                                Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                                    SearchEmptyState("Препараты не найдены", Icons.Default.MedicalServices)
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(bottom = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    items(medicationResults, key = { it.id }) { medication ->
                                        MedicationListItem(medication = medication, onClick = { selectedMedication = medication })
                                    }
                                }
                            }
                        }

                        2 -> {
                            val filteredRecipes =
                                if (searchQuery.isBlank()) {
                                    allRecipes
                                } else {
                                    allRecipes.filter {
                                        it.patient_name.contains(searchQuery, ignoreCase = true) ||
                                            it.patient_iin.contains(searchQuery)
                                    }
                                }
                            if (filteredRecipes.isEmpty() && !isSearching) {
                                Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                                    SearchEmptyState(
                                        if (searchQuery.isNotEmpty()) "Рецепты не найдены" else "История рецептов пуста",
                                        Icons.Default.ReceiptLong,
                                    )
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(bottom = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    items(filteredRecipes, key = { it.id }) { recipe ->
                                        SearchRecipeHistoryCard(recipe = recipe, onClick = { selectedRecipe = recipe })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedPatient != null) {
        val patientRecipes = allRecipes.filter { it.patient_iin == selectedPatient!!.iin }
        PatientProfileSheet(
            patient = selectedPatient!!,
            recipes = patientRecipes,
            onRecipeClick = { recipe ->
                selectedPatient = null
                selectedRecipe = recipe
            },
            onDismiss = { selectedPatient = null },
        )
    }

    if (selectedMedication != null) {
        MedicationInfoSheet(medication = selectedMedication!!, onDismiss = { selectedMedication = null })
    }

    if (selectedRecipe != null) {
        SearchRecipeDetailsDialog(recipe = selectedRecipe!!, onDismiss = { selectedRecipe = null }, viewModel = viewModel)
    }
}

@Composable
fun MedicationSkeletonList() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "alpha",
    )

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(6) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)),
            )
        }
    }
}

@Composable
fun SearchEmptyState(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun PatientListItem(
    patient: Patient,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        ListItem(
            headlineContent = { Text(patient.full_name, fontWeight = FontWeight.Bold) },
            supportingContent = { Text("ИИН: ${patient.iin}") },
            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
fun MedicationListItem(
    medication: Medication,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        ListItem(
            headlineContent = { Text(medication.name, fontWeight = FontWeight.Bold) },
            supportingContent = { Text(medication.activeSubstance) },
            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
fun SearchRecipeHistoryCard(
    recipe: Recipe,
    onClick: () -> Unit,
) {
    val sdf = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
    val dateStr = sdf.format(Date(recipe.date))
    val expireStr = sdf.format(Date(recipe.expire_date))
    val recipeNum = recipe.id.takeLast(4).uppercase()

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Рецепт №$recipeNum",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.patient_name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            val isActive = recipe.isActive
            val badgeContainerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
            val badgeContentColor = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeContainerColor)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = if (isActive) "Активен" else "Истек",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = badgeContentColor,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "до $expireStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeContentColor.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileSheet(
    patient: Patient,
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onDismiss: () -> Unit,
) {
    var recipesExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(text = patient.full_name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = "ИИН: ${patient.iin}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Пол: ${patient.gender} • Рождение: ${patient.birth_date}", color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(24.dp))

            Text("Примечание", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(text = patient.allergies.ifEmpty { "Нет примечаний" }, style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { recipesExpanded = !recipesExpanded },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Выписанные рецепты (${recipes.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = if (recipesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                        )
                    }

                    AnimatedVisibility(visible = recipesExpanded) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            if (recipes.isEmpty()) {
                                Text("История пуста", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                recipes.forEach { recipe ->
                                    val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(Date(recipe.date))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { onRecipeClick(recipe) }.padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(dateStr, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(
                                            "№${recipe.id.takeLast(4).uppercase()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Закрыть")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationInfoSheet(
    medication: Medication,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(text = medication.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = medication.activeSubstance, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)

            if (medication.category.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = medication.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Формы выпуска и дозировки", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${medication.forms.joinToString(", ")}\nДоступно: ${medication.availableDosages.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Фармакологическое действие", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(medication.description.ifEmpty { "Нет описания" }, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Показания", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(medication.indications.ifEmpty { "Не указаны" }, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Противопоказания", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(4.dp))
            Text(medication.contraindications.ifEmpty { "Не указаны" }, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Побочные действия", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(medication.sideEffects.ifEmpty { "Не указаны" }, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Закрыть")
            }
        }
    }
}

@Composable
fun SearchRecipeDetailsDialog(
    recipe: Recipe,
    onDismiss: () -> Unit,
    viewModel: SearchViewModel,
) {
    val qrUrl = "https://e-recepta-mbfodsfs.vercel.app/recipes/${recipe.id}/qr"
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } },
        title = { Text("Детали рецепта", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = qrUrl,
                        contentDescription = "QR Code",
                        modifier = Modifier.size(200.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Пациент: ${recipe.patient_name}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("ИИН: ${recipe.patient_iin}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                if (recipe.medications.isEmpty()) {
                    Text("• Нет указанных препаратов", modifier = Modifier.padding(vertical = 4.dp))
                } else {
                    recipe.medications.forEach { med ->
                        Text("• ${med.name}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                        val desc = if (med.dosageValue.isNotBlank()) "${med.dosageValue} ${med.dosageUnit} × ${med.frequency}/день — ${med.durationValue} ${med.durationUnit}" else med.dosageUnit
                        Text("  $desc", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 4.dp))
                    }
                }

                if (recipe.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Рекомендации: ${recipe.notes}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
    )
}
