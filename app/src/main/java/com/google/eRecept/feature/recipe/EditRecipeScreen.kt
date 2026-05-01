package com.google.eRecept.feature.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.eRecept.R
import com.google.eRecept.core.ui.components.CustomSegmentedControl
import com.google.eRecept.data.model.MedicationItem
import com.google.eRecept.feature.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreen(
    viewModel: RecipeViewModel,
    onNavigateBack: () -> Unit,
    homeViewModel: HomeViewModel
) {
    val focusManager = LocalFocusManager.current

    val draftPatientIin by viewModel.draftPatientIin.collectAsStateWithLifecycle()
    val draftMedications by viewModel.draftMedications.collectAsStateWithLifecycle()
    val draftNotes by viewModel.draftNotes.collectAsStateWithLifecycle()
    val draftExpireDays by viewModel.draftExpireDays.collectAsStateWithLifecycle()
    val isCreating by viewModel.isCreating.collectAsStateWithLifecycle()

    val patientResult by homeViewModel.searchPatientResult.collectAsStateWithLifecycle()

    LaunchedEffect(draftPatientIin) {
        if (draftPatientIin.length == 12) {
            homeViewModel.searchPatient(draftPatientIin)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактирование рецепта") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearDraft() // Clear state when leaving
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Locked IIN Field
                OutlinedTextField(
                    value = draftPatientIin,
                    onValueChange = { },
                    label = { Text(stringResource(R.string.patient_iin)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    readOnly = true, // Prevents typing
                    enabled = false, // Visually indicates it cannot be changed
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                if (patientResult != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
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
                                            text = patientResult!!.full_name.firstOrNull()
                                                ?.toString() ?: "?",
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
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                            alpha = 0.7f
                                        ),
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                InfoTag(
                                    label = "Пол",
                                    value = patientResult!!.gender ?: "Не указан"
                                )
                                InfoTag(
                                    label = "Дата рожд.",
                                    value = patientResult!!.birth_date ?: "Не указана"
                                )
                            }

                            val note = patientResult!!.allergies ?: ""
                            if (note.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            MaterialTheme.colorScheme.errorContainer.copy(
                                                alpha = 0.3f
                                            )
                                        )
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

                // Reuse the SmartMedicationRow from CreateRecipeScreen.kt
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

            Button(
                onClick = {
                    viewModel.saveRecipe("")
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isCreating && draftMedications.any { it.id.isNotBlank() },
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Сохранить изменения", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}