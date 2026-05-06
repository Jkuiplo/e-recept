package com.google.eRecept.feature.recipe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.eRecept.R
import com.google.eRecept.core.ui.components.CustomSegmentedControl
import com.google.eRecept.core.ui.components.PatientInfoCard
import com.google.eRecept.core.ui.components.SkeletonList
import com.google.eRecept.data.model.MedicationItem
import com.google.eRecept.feature.home.HomeViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    viewModel: RecipeViewModel,
    homeViewModel: HomeViewModel,
    onNavigateBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    val draftPatientIin by viewModel.draftPatientIin.collectAsStateWithLifecycle()
    val draftMedications by viewModel.draftMedications.collectAsStateWithLifecycle()
    val draftNotes by viewModel.draftNotes.collectAsStateWithLifecycle()
    val draftExpireDays by viewModel.draftExpireDays.collectAsStateWithLifecycle()

    val patientResult by homeViewModel.searchPatientResult.collectAsStateWithLifecycle()
    val isSearchingPatient by homeViewModel.isSearching.collectAsStateWithLifecycle()
    val isCreating by viewModel.isCreating.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var showValidationErrors by remember { mutableStateOf(false) }

    LaunchedEffect(draftPatientIin) {
        if (draftPatientIin.length == 12) {
            homeViewModel.searchPatient(draftPatientIin)
        } else if (draftPatientIin.isEmpty()) {
            homeViewModel.clearSearchResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.write_prescription)) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.closeCreateSheet()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (isLoading) {
                    SkeletonList(itemCount = 3, itemHeight = 120.dp)
                } else {
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
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        trailingIcon = {
                            if (isSearchingPatient) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else if (draftPatientIin.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateDraftIin("") }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.clear)
                                    )
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
                        PatientInfoCard(
                            patient = patientResult!!,
                            onClick = { /* Можно добавить навигацию в детали */ }
                        )
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
                            showError = showValidationErrors && med.name.isBlank(),
                            onMedicationChange = { updatedMed ->
                                val newList = draftMedications.toMutableList()
                                newList[index] = updatedMed
                                viewModel.updateDraftMedications(newList)
                            },
                            onRemove = if (draftMedications.size > 1) {
                                {
                                    val newList = draftMedications.toMutableList()
                                    newList.removeAt(index)
                                    viewModel.updateDraftMedications(newList)
                                }
                            } else null,
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

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            if (!isLoading) {
                val allMeds by viewModel.allMedications.collectAsStateWithLifecycle()
                val isAllMedicationsValid = draftMedications.all { med ->
                    med.name.isNotBlank() && allMeds.any { it.name.equals(med.name, ignoreCase = true) }
                }

                Button(
                    onClick = {
                        if (!isAllMedicationsValid) {
                            showValidationErrors = true
                        } else {
                            viewModel.saveRecipe(patientResult?.full_name ?: "Неизвестно")
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isCreating && draftPatientIin.length == 12 && patientResult != null && draftMedications.isNotEmpty() && isAllMedicationsValid,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartMedicationRow(
    index: Int,
    medication: MedicationItem,
    viewModel: RecipeViewModel,
    showError: Boolean = false,
    onMedicationChange: (MedicationItem) -> Unit,
    onRemove: (() -> Unit)?,
) {
    var nameExpanded by remember { mutableStateOf(false) }
    var fieldSize by remember { mutableStateOf(Size.Zero) }
    val suggestions by viewModel.medicationSuggestions.collectAsStateWithLifecycle()
    val allMeds by viewModel.allMedications.collectAsStateWithLifecycle()
    val density = LocalDensity.current

    val isMedicationValid = medication.name.isBlank() || allMeds.any { it.name.equals(medication.name, ignoreCase = true) }
    val showErrorWithText = showError || (medication.name.isNotBlank() && !isMedicationValid)

    val dosageUnits = listOf("мг", "мл", "таб")
    val frequencies = listOf("1×", "2×", "3×", "4×")
    val durationUnits = listOf("дней", "нед", "мес")

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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f
            )
        ),
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
                OutlinedTextField(
                    value = medication.name,
                    onValueChange = {
                        onMedicationChange(medication.copy(name = it, id = ""))
                        viewModel.searchMedications(it)
                        nameExpanded = true
                    },
                    label = { Text(stringResource(R.string.medication_name)) },
                    modifier = Modifier.fillMaxWidth()
                        .onGloballyPositioned { fieldSize = it.size.toSize() },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = showErrorWithText,
                    supportingText = if (showErrorWithText) {
                        {
                            Text(
                                if (medication.name.isBlank()) "Заполните все поля"
                                else "Медикамент не найден"
                            )
                        }
                    } else null
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
                                    Text(
                                        suggestion.activeSubstance,
                                        style = MaterialTheme.typography.bodySmall
                                    )
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
                    value = medication.dosageValue,
                    onValueChange = {
                        onMedicationChange(
                            medication.copy(
                                dosageValue = safeNumberInput(
                                    it,
                                    allowDecimal = true
                                )
                            )
                        )
                    },
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
                    value = medication.durationValue,
                    onValueChange = {
                        onMedicationChange(
                            medication.copy(
                                durationValue = safeNumberInput(
                                    it,
                                    allowDecimal = false
                                )
                            )
                        )
                    },
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
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onDecrement,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Меньше",
                tint = MaterialTheme.colorScheme.primary
            )
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
            Icon(
                Icons.Default.Add,
                contentDescription = "Больше",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun InfoTag(
    label: String,
    value: String,
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
