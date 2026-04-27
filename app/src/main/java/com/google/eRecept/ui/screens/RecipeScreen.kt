package com.google.eRecept.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.google.eRecept.R
import com.google.eRecept.data.MedicationItem
import com.google.eRecept.data.Recipe
import com.google.eRecept.ui.viewmodels.HomeViewModel
import com.google.eRecept.ui.viewmodels.RecipeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    viewModel: RecipeViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
) {
    val focusManager = LocalFocusManager.current
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val showCreateSheet by viewModel.showCreateSheet.collectAsStateWithLifecycle()

    val draftPatientIin by viewModel.draftPatientIin.collectAsStateWithLifecycle()
    val draftMedications by viewModel.draftMedications.collectAsStateWithLifecycle()
    val draftNotes by viewModel.draftNotes.collectAsStateWithLifecycle()
    val draftExpireDays by viewModel.draftExpireDays.collectAsStateWithLifecycle()

    val patientResult by homeViewModel.searchPatientResult.collectAsStateWithLifecycle()
    val isSearchingPatient by homeViewModel.isSearching.collectAsStateWithLifecycle()

    LaunchedEffect(draftPatientIin) {
        if (draftPatientIin.length == 12) {
            homeViewModel.searchPatient(draftPatientIin)
        } else if (draftPatientIin.isEmpty()) {
            homeViewModel.clearSearchResult()
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.openCreateSheet() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            },
        ) { padding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp),
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.recipes_title),
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(24.dp))

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (recipes.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyRecipesState()
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 88.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(recipes, key = { it.id }) { recipe ->
                                RecipeHistoryCard(recipe = recipe, onClick = { selectedRecipe = recipe }, viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedRecipe != null) {
        RecipeDetailsDialog(
            recipe = selectedRecipe!!,
            onDismiss = { selectedRecipe = null },
            viewModel = viewModel,
        )
    }

    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeCreateSheet() },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxHeight(),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 20.dp)
                                .verticalScroll(rememberScrollState()),
                    ) {
                        Text(
                            text = stringResource(R.string.write_prescription),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = draftPatientIin,
                            onValueChange = {
                                if (it.length <= 12 && it.all { char -> char.isDigit() }) {
                                    viewModel.updateDraftIin(it)
                                }
                            },
                            label = { Text(stringResource(R.string.patient_iin)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            trailingIcon = {
                                if (isSearchingPatient) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                } else if (draftPatientIin.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateDraftIin("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear))
                                    }
                                }
                            },
                        )
                        if (draftPatientIin.length == 12 && !isSearchingPatient && patientResult == null) {
                            Text(
                                text = stringResource(R.string.patient_not_found),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                            )
                        } else if (patientResult != null && draftPatientIin.length == 12) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                    ),
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(48.dp),
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = patientResult!!.full_name.firstOrNull()?.toString() ?: "?",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = MaterialTheme.colorScheme.onSecondary,
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = patientResult!!.full_name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Text(
                                                text = "ИИН: $draftPatientIin",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                            )
                                        }
                                    }

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                                    )

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        InfoTag(label = "Пол", value = patientResult!!.gender ?: "Не указан")
                                        InfoTag(label = "Дата рожд.", value = patientResult!!.birth_date ?: "Не указана")
                                    }

                                    val note = patientResult!!.allergies ?: ""
                                    if (note.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                                                    .padding(8.dp),
                                        ) {
                                            Row {
                                                Icon(
                                                    Icons.Default.Warning,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.error,
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = note,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = stringResource(R.string.prescriptions_list),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        draftMedications.forEachIndexed { index, med ->
                            SmartMedicationRow(
                                index = index,
                                medication = med,
                                viewModel = viewModel,
                                onMedicationChange = { updatedMed ->
                                    val newList = draftMedications.toMutableList()
                                    newList[index] = updatedMed
                                    viewModel.updateDraftMedications(newList)
                                },
                                onRemove =
                                    if (draftMedications.size > 1) {
                                        {
                                            val newList = draftMedications.toMutableList()
                                            newList.removeAt(index)
                                            viewModel.updateDraftMedications(newList)
                                        }
                                    } else {
                                        null
                                    },
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        TextButton(
                            onClick = { viewModel.updateDraftMedications(draftMedications + MedicationItem()) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.add_medication))
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.recipe_settings),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(stringResource(R.string.recipe_validity_days), style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        CustomSegmentedControl(
                            options = listOf("10", "15", "30", "60"),
                            selectedOption = draftExpireDays.toString(),
                            onOptionSelected = { viewModel.updateDraftExpireDays(it.toIntOrNull() ?: 30) },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = draftNotes,
                            onValueChange = { viewModel.updateDraftNotes(it) },
                            label = { Text(stringResource(R.string.general_recommendations)) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                            shape = RoundedCornerShape(12.dp),
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                        ) {
                            Column {
                                Text(
                                    stringResource(R.string.final_recipe),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                draftMedications.forEach { med ->
                                    if (med.name.isNotBlank()) {
                                        Text("• ${med.name}", fontWeight = FontWeight.Bold)
                                        Text("  ${med.summary}", style = MaterialTheme.typography.bodySmall)
                                        if (med.note.isNotBlank()) {
                                            Text(
                                                stringResource(R.string.note_format, med.note),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f),
                                )
                                Text(
                                    stringResource(R.string.valid_for_days, draftExpireDays),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                    val isCreating by viewModel.isCreating.collectAsStateWithLifecycle()
                    val editingRecipeId by viewModel.editingRecipeId.collectAsStateWithLifecycle()
                    val isEditing = editingRecipeId != null

                    Button(
                        onClick = { viewModel.saveRecipe(patientResult?.full_name ?: "Неизвестно") },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                                .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled =
                            !isCreating &&
                                draftPatientIin.length == 12 &&
                                patientResult != null &&
                                draftMedications.any { it.id.isNotBlank() },
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(stringResource(R.string.write_prescription), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeHistoryCard(
    recipe: Recipe,
    onClick: () -> Unit,
    viewModel: RecipeViewModel,
) {
    val sdf = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
    val dateStr = sdf.format(Date(recipe.date))
    val expireStr = sdf.format(Date(recipe.expire_date))
    val recipeNum = recipe.id.takeLast(4).uppercase()

    var showMenu by remember { mutableStateOf(false) }
    var showRevokeConfirm by remember { mutableStateOf(false) }
    val isRevoking by viewModel.isRevoking.collectAsStateWithLifecycle(initialValue = false)

    // Определяем логику статуса
    val isExpired = recipe.expire_date < System.currentTimeMillis()
    val displayStatus =
        when {
            recipe.status == "Активен" && isExpired -> stringResource(R.string.status_expired)
            recipe.status == "Активен" -> stringResource(R.string.status_active)
            else -> recipe.status // Если пришел "Отозван" или "Неактивен" с бэкенда
        }
    val isGreenBadge = recipe.status == "Активен" && !isExpired

    val badgeContainerColor = if (isGreenBadge) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val badgeContentColor = if (isGreenBadge) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth().padding(
                    start = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp,
                    end = if (isGreenBadge) 4.dp else 16.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.recipe_number_format, recipeNum),
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

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeContainerColor)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = displayStatus,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = badgeContentColor,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.until_date_format, expireStr),
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeContentColor.copy(alpha = 0.8f),
                )
            }
            if (isGreenBadge) {
                Box(modifier = Modifier.padding(start = 4.dp)) {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Опции")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Редактировать") },
                            onClick = {
                                showMenu = false
                                viewModel.openEditSheet(recipe)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Отозвать", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                showRevokeConfirm = true
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
    if (showRevokeConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isRevoking) showRevokeConfirm = false },
            title = { Text("Отозвать рецепт?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Вы уверены, что хотите деактивировать этот рецепт? После отзыва пациент не сможет получить по нему препараты в аптеке.",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.revokeRecipe(recipe.id) {
                            showRevokeConfirm = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    if (isRevoking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Отозвать", color = MaterialTheme.colorScheme.onError)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevokeConfirm = false }, enabled = !isRevoking) {
                    Text("Отмена")
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartMedicationRow(
    index: Int,
    medication: MedicationItem,
    viewModel: RecipeViewModel,
    onMedicationChange: (MedicationItem) -> Unit,
    onRemove: (() -> Unit)?,
) {
    var nameExpanded by remember { mutableStateOf(false) }
    var fieldSize by remember { mutableStateOf(Size.Zero) }
    val suggestions by viewModel.medicationSuggestions.collectAsStateWithLifecycle()
    val density = LocalDensity.current

    val dosageUnits = listOf("мг", "мл", "таб")
    val frequencies = listOf("1×", "2×", "3×", "4×")
    val durationUnits = listOf("дн.", "нед", "мес")

    fun safeNumberInput(
        input: String,
        allowDecimal: Boolean,
    ): String {
        var filtered = input.filter { it.isDigit() || (allowDecimal && it == '.') }
        if (allowDecimal) {
            val firstDotIndex = filtered.indexOf('.')
            if (firstDotIndex != -1) {
                val beforeDot = filtered.substring(0, firstDotIndex)
                val afterDot = filtered.substring(firstDotIndex + 1).replace(".", "")
                filtered = "$beforeDot.$afterDot"
            }
            if (filtered.startsWith(".")) {
                filtered = "0$filtered"
            }
        }
        return filtered
    }

    fun formatNumber(value: Double): String {
        val rounded = (value * 10.0).roundToInt() / 10.0
        return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.medication_number, index + 1),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (onRemove != null) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box {
                val isMedicationNotSelected = medication.name.isNotBlank() && medication.id.isBlank()

                OutlinedTextField(
                    value = medication.name,
                    onValueChange = {
                        onMedicationChange(medication.copy(name = it, id = ""))
                        viewModel.searchMedications(it)
                        nameExpanded = true
                    },
                    label = { Text(stringResource(R.string.medication_name)) },
                    isError = isMedicationNotSelected,
                    supportingText = {
                        if (isMedicationNotSelected) {
                            Text(stringResource(R.string.medication_required))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().onGloballyPositioned { fieldSize = it.size.toSize() },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
                DropdownMenu(
                    expanded = nameExpanded && suggestions.isNotEmpty(),
                    onDismissRequest = { nameExpanded = false },
                    properties = PopupProperties(focusable = false),
                    modifier = Modifier.width(with(density) { fieldSize.width.toDp() }),
                ) {
                    suggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(suggestion.name, fontWeight = FontWeight.Bold)
                                    Text(suggestion.activeSubstance, style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            onClick = {
                                onMedicationChange(
                                    medication.copy(
                                        id = suggestion.id,
                                        name = suggestion.name,
                                    ),
                                )
                                nameExpanded = false
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                stringResource(R.string.dosage),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StepperInput(
                    value = medication.dosageValue.ifEmpty { "1" },
                    onValueChange = { onMedicationChange(medication.copy(dosageValue = safeNumberInput(it, allowDecimal = true))) },
                    onDecrement = {
                        val current = medication.dosageValue.toDoubleOrNull() ?: 1.0
                        val step = if (current % 1.0 == 0.0) 1.0 else 0.1
                        val newValue = if (current - step < 0.1) 0.1 else current - step
                        onMedicationChange(medication.copy(dosageValue = formatNumber(newValue)))
                    },
                    onIncrement = {
                        val current = medication.dosageValue.toDoubleOrNull() ?: 0.0
                        val step = if (current % 1.0 == 0.0) 1.0 else 0.1
                        onMedicationChange(medication.copy(dosageValue = formatNumber(current + step)))
                    },
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                )

                CustomSegmentedControl(
                    options = dosageUnits,
                    selectedOption = medication.dosageUnit,
                    onOptionSelected = { onMedicationChange(medication.copy(dosageUnit = it)) },
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                stringResource(R.string.frequency),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            CustomSegmentedControl(
                options = frequencies,
                selectedOption = medication.frequency,
                onOptionSelected = { onMedicationChange(medication.copy(frequency = it)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                stringResource(R.string.duration),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StepperInput(
                    value = medication.durationValue.ifEmpty { "1" },
                    onValueChange = { onMedicationChange(medication.copy(durationValue = safeNumberInput(it, allowDecimal = false))) },
                    onDecrement = {
                        val current = medication.durationValue.toIntOrNull() ?: 1
                        val newValue = if (current > 1) current - 1 else 1
                        onMedicationChange(medication.copy(durationValue = newValue.toString()))
                    },
                    onIncrement = {
                        val current = medication.durationValue.toIntOrNull() ?: 0
                        onMedicationChange(medication.copy(durationValue = (current + 1).toString()))
                    },
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                )

                CustomSegmentedControl(
                    options = durationUnits,
                    selectedOption = medication.durationUnit,
                    onOptionSelected = { onMedicationChange(medication.copy(durationUnit = it)) },
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = medication.note,
                onValueChange = { onMedicationChange(medication.copy(note = it)) },
                label = { Text(stringResource(R.string.special_instructions)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )
        }
    }
}

@Composable
fun RecipeDetailsDialog(
    recipe: Recipe,
    onDismiss: () -> Unit,
    viewModel: RecipeViewModel,
) {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    val dateStr = sdf.format(Date(recipe.date))
    val expireStr = sdf.format(Date(recipe.expire_date))

    val qrUrl = "https://e-recepta.vercel.app/recipes/${recipe.id}/qr"

    var showMenu by remember { mutableStateOf(false) }
    var showRevokeConfirm by remember { mutableStateOf(false) }
    val isRevoking by viewModel.isRevoking.collectAsStateWithLifecycle(initialValue = false)

    // Вычисляем статусы
    val isExpired = recipe.expire_date < System.currentTimeMillis()
    val displayStatusCaps =
        when {
            recipe.status == "Активен" && isExpired -> stringResource(R.string.status_expired_caps)
            recipe.status == "Активен" -> stringResource(R.string.status_active_caps)
            else -> recipe.status.uppercase()
        }
    val isGreenBadge = recipe.status == "Активен" && !isExpired

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.recipe_details), fontWeight = FontWeight.Bold)

                if (isGreenBadge) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Опции")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Редактировать") },
                                onClick = {
                                    showMenu = false
                                    onDismiss()
                                    viewModel.openEditSheet(recipe)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Отозвать", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    showRevokeConfirm = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                },
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    SubcomposeAsyncImage(
                        model = qrUrl,
                        contentDescription = stringResource(R.string.qr_code),
                        modifier = Modifier.size(200.dp),
                        contentScale = ContentScale.Fit,
                        loading = {
                            CircularProgressIndicator(modifier = Modifier.padding(64.dp))
                        },
                        error = {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = stringResource(R.string.loading_error),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp),
                            )
                        },
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                val badgeColor = if (isGreenBadge) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                val textColor = if (isGreenBadge) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            displayStatusCaps,
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.prescribed_date, dateStr), style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    stringResource(R.string.valid_until_date, expireStr),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.patient_name_format, recipe.patient_name),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(stringResource(R.string.iin, recipe.patient_iin), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                recipe.medications.forEach { med ->
                    Text("• ${med.name}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                    Text("  ${med.summary}", style = MaterialTheme.typography.bodySmall)
                    if (med.note.isNotBlank()) {
                        Text(
                            stringResource(R.string.note_format, med.note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                if (recipe.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.general_recommendations_format, recipe.notes), style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
    )

    if (showRevokeConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isRevoking) showRevokeConfirm = false },
            title = { Text("Отозвать рецепт?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Вы уверены, что хотите деактивировать этот рецепт? После отзыва пациент не сможет получить по нему препараты в аптеке.",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.revokeRecipe(recipe.id) {
                            showRevokeConfirm = false
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    if (isRevoking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Отозвать", color = MaterialTheme.colorScheme.onError)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRevokeConfirm = false },
                    enabled = !isRevoking,
                ) {
                    Text("Отмена")
                }
            },
        )
    }
}

@Composable
fun EmptyRecipesState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = 100.dp),
    ) {
        Icon(
            imageVector = Icons.Default.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.no_recipes_yet),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.no_recipes_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun CustomSegmentedControl(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .height(IntrinsicSize.Min)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selectedOption

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing),
                label = "segmentBackground",
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing),
                label = "segmentText",
            )

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(color = backgroundColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            onOptionSelected(option)
                        }.padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }

            if (index < options.size - 1) {
                VerticalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp,
                )
            }
        }
    }
}

@Composable
fun StepperInput(
    value: String,
    onValueChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onDecrement,
            modifier = Modifier.weight(1f),
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Меньше", tint = MaterialTheme.colorScheme.primary)
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1.5f),
            textStyle =
                LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )

        IconButton(
            onClick = onIncrement,
            modifier = Modifier.weight(1f),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Больше", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun InfoTag(
    label: String,
    value: String,
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
