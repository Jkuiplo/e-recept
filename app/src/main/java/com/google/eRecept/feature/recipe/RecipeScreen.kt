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
import com.google.eRecept.core.ui.components.PatientInfoCard
import com.google.eRecept.data.model.Recipe
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.eRecept.core.ui.components.RecipeCard
import androidx.compose.material.icons.filled.Add
import com.google.eRecept.core.ui.components.RecipeDetailsBottomSheet
import com.google.eRecept.data.model.Patient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    viewModel: RecipeViewModel,
    onNavigateToCreateRecipe: () -> Unit,
    onEditRecipe: () -> Unit,
    onNavigateToPatientDetails: (String) -> Unit,
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
        RecipeDetailsBottomSheet(
            recipe = selectedRecipe!!,
            onDismiss = { selectedRecipe = null },
            viewModel = viewModel,
            onEdit = onEditRecipe,
            onNavigateToPatientDetails = onNavigateToPatientDetails
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
            text = "Сегодня рецептов нет",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Вы еще не выписали ни одного рецепта за сегодняшний день.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
