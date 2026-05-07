package com.google.eRecept.feature.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.eRecept.R
import com.google.eRecept.core.ui.components.SkeletonList
import com.google.eRecept.data.model.Patient
import com.google.eRecept.data.model.Recipe
import com.google.eRecept.feature.recipe.RecipeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PatientsTabContent(
    viewModel: SearchViewModel,
    recipeViewModel: RecipeViewModel,
    searchQuery: String,
    isSearching: Boolean,
    onNavigateToPatientDetails: (String) -> Unit
) {
    val patientResults by viewModel.patientResults.collectAsStateWithLifecycle()

    if (isSearching && patientResults.isEmpty()) {
        SkeletonList()
    } else if (patientResults.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
            if (searchQuery.isNotEmpty()) {
                SearchEmptyState(stringResource(R.string.patients_not_found), Icons.Default.PersonOff)
            } else {
                SearchEmptyState("Список пациентов пуст", Icons.Default.GroupOff)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(patientResults, key = { it.iin }) { patient ->
                PatientListItem(patient = patient, onClick = { onNavigateToPatientDetails(patient.iin) })
            }
        }
    }
}

@Composable
fun PatientListItem(
    patient: Patient,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = patient.full_name,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            supportingContent = { Text(stringResource(R.string.iin, patient.iin)) },
            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatientProfileSheet(
    patient: Patient,
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onDismiss: () -> Unit,
) {
    var recipesExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(text = patient.full_name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = stringResource(R.string.iin, patient.iin), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = stringResource(R.string.gender_and_birth, patient.gender, patient.birth_date),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(stringResource(R.string.note), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(text = patient.allergies.ifEmpty { stringResource(R.string.no_notes) }, style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { recipesExpanded = !recipesExpanded },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.prescribed_recipes_count, recipes.size),
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
                                Text(stringResource(R.string.history_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                recipes.forEach { recipe ->
                                    val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(Date(recipe.date))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onRecipeClick(recipe) }
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(dateStr, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(
                                            stringResource(R.string.number_format, recipe.id.takeLast(4).uppercase()),
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
                Text(stringResource(R.string.close))
            }
        }
    }
}