package com.google.eRecept.feature.recipe

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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.google.eRecept.R
import com.google.eRecept.data.model.Recipe
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.eRecept.core.ui.components.RecipeCard
import androidx.compose.material.icons.filled.Add

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    viewModel: RecipeViewModel,
    onNavigateToCreateRecipe: () -> Unit,
    onEditRecipe: () -> Unit,
) {
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    val allRecipes by viewModel.recipes.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val todayRecipes = remember(allRecipes) {
        val todayStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        allRecipes.filter { it.date >= todayStart }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        viewModel.updateDraftIin("")
                        onNavigateToCreateRecipe()
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp),
            ) {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (todayRecipes.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyRecipesState()
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(todayRecipes, key = { it.id }) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    onClick = { selectedRecipe = recipe },
                                    viewModel = viewModel,
                                    onEdit = onEditRecipe
                                )
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
            onEdit = onEditRecipe
        )
    }
}

@Composable
fun RecipeDetailsDialog(
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

    // Вычисляем статусы
    val isExpired = recipe.expire_date < System.currentTimeMillis()
    val displayStatusCaps =
        when {
            recipe.status == "Активен" && isExpired -> stringResource(R.string.status_expired_caps)
            recipe.status == "Активен" -> stringResource(R.string.status_active_caps)
            else -> recipe.status.uppercase()
        }
    val isGreenBadge = recipe.status == "Активен" && !isExpired
    val isRevoked = recipe.status.equals("Отозван", ignoreCase = true)

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
                if (isRevoked) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "Рецепт отозван",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        )
                    }
                }
                else {
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
                if (isRevoked) {
                    Text(
                        text = "Отозван: Сегодня",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                else{
                    Text(
                        stringResource(R.string.valid_until_date, expireStr),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

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
