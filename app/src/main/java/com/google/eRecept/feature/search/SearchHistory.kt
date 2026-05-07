package com.google.eRecept.feature.search

import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.google.eRecept.R
import com.google.eRecept.core.ui.components.RecipeCard
import com.google.eRecept.core.ui.components.RecipeDetailsBottomSheet
import com.google.eRecept.core.ui.components.SkeletonList
import com.google.eRecept.data.model.Recipe
import com.google.eRecept.feature.recipe.RecipeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryTabContent(
    recipeViewModel: RecipeViewModel,
    searchQuery: String,
    isSearching: Boolean,
    onEditRecipe: () -> Unit,
    onNavigateToPatientDetails: (String) -> Unit,
) {
    val allRecipes by recipeViewModel.recipes.collectAsStateWithLifecycle()
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }

    val filteredRecipes = remember(allRecipes, searchQuery) {
        if (searchQuery.isBlank()) {
            allRecipes
        } else {
            val lowerQuery = searchQuery.lowercase()
            allRecipes.filter { recipe ->
                recipe.id.lowercase().contains(lowerQuery) ||
                        recipe.patient_name.lowercase().contains(lowerQuery) ||
                        recipe.patient_iin.contains(searchQuery)
            }
        }
    }

    if (isSearching && filteredRecipes.isEmpty()) {
        SkeletonList()
    } else if (filteredRecipes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
            if (searchQuery.isNotEmpty()) {
                SearchEmptyState(stringResource(R.string.recipes_not_found), Icons.Default.SearchOff)
            } else {
                SearchEmptyState(stringResource(R.string.recipe_history_empty), Icons.Default.ReceiptLong)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(filteredRecipes, key = { it.id }) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onClick = { selectedRecipe = recipe },
                    viewModel = recipeViewModel,
                    onEdit = onEditRecipe,
                )
            }
        }
    }

    if (selectedRecipe != null) {
        RecipeDetailsBottomSheet(
            recipe = selectedRecipe!!,
            onDismiss = { selectedRecipe = null },
            viewModel = recipeViewModel,
            onEdit = onEditRecipe,
            onNavigateToPatientDetails = onNavigateToPatientDetails
        )
    }
}

@Composable
fun SearchRecipeDetailsDialog(
    recipe: Recipe,
    onDismiss: () -> Unit,
    viewModel: RecipeViewModel,
    onEdit: () -> Unit,
) {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    val dateStr = sdf.format(Date(recipe.date))
    val expireStr = sdf.format(Date(recipe.expire_date))
    val qrUrl = "https://e-recepta.vercel.app/recipes/${recipe.id}/qr"

    var showMenu by remember { mutableStateOf(false) }
    var showRevokeConfirm by remember { mutableStateOf(false) }
    val isRevoking by viewModel.isRevoking.collectAsStateWithLifecycle(initialValue = false)

    val isExpired = recipe.expire_date < System.currentTimeMillis()
    val displayStatusCaps = when {
        recipe.status == "Активен" && isExpired -> stringResource(R.string.status_expired_caps)
        recipe.status == "Активен" -> stringResource(R.string.status_active_caps)
        else -> recipe.status.uppercase()
    }
    val isGreenBadge = recipe.status == "Активен" && !isExpired
    val isRevoked = recipe.status.equals("Отозван", ignoreCase = true)

    val badgeColor = if (isGreenBadge) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val textColor = if (isGreenBadge) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.recipe_details),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
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

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.prescribed_date, dateStr),
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (isRevoked) {
                        Text(
                            text = "Отозван: Сегодня",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    } else {    Text(
                            stringResource(R.string.valid_until_date, expireStr),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }

                if (isGreenBadge) {
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.offset(x = 12.dp, y = (-12).dp)) {
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
                                    onEdit()
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
                Spacer(modifier = Modifier.height(8.dp))
                if (isRevoked) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "Рецепт отозван",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    SubcomposeAsyncImage(
                        model = qrUrl,
                        contentDescription = stringResource(R.string.qr_code),
                        modifier = Modifier.size(180.dp),
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
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    stringResource(R.string.patient_name_format, recipe.patient_name),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(stringResource(R.string.iin, recipe.patient_iin), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                if (recipe.medications.isEmpty()) {
                    Text(stringResource(R.string.no_medications_specified), modifier = Modifier.padding(vertical = 4.dp))
                } else {
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
                }

                if (recipe.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.recommendations_format, recipe.notes), style = MaterialTheme.typography.bodyMedium)
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